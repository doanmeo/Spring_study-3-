package com.saleticket.exam1.service;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.saleticket.exam1.dto.request.AuthenticationRequest;
import com.saleticket.exam1.dto.request.LogoutRequest;
import com.saleticket.exam1.dto.request.RefreshRequest;
import com.saleticket.exam1.dto.response.ApiResponse;
import com.saleticket.exam1.dto.response.AuthenticationResponse;
import com.saleticket.exam1.entity.InvalidatedToken;
import com.saleticket.exam1.entity.User;
import com.saleticket.exam1.exception.AppException;
import com.saleticket.exam1.exception.ErrorCode;
import com.saleticket.exam1.respository.InvalidatedTokenRepository;
import com.saleticket.exam1.respository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {
    @Value("${jwt.signerKey}") // lấy giá trị của khóa bí mật từ file cấu hình (application.properties hoặc
                               // application.yml) de sử dụng trong việc tạo và xác minh token JWT
    private String SECRET_KEY;

    @NonFinal
    @Value("${jwt.valid-duration}") // lấy giá trị của khóa bí mật từ file cấu hình (application.properties hoặc
                                    // application.yml) de sử dụng trong việc tạo và xác minh token JWT
    protected Long VALID_DURATION;

    @NonFinal
    @Value("${jwt.refresh-duration}") // lấy giá trị của khóa bí mật từ file cấu hình (application.properties hoặc
                                      // application.yml) de sử dụng trong việc tạo và xác minh token JWT
    protected Long REFRESH_DURATION;

    private final UserRepository userRepository;
    private final InvalidatedTokenRepository invalidatedTokenRepository;

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        var user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        boolean isAuthenticated = encoder.matches(request.password(), user.getPassword());
        if (!isAuthenticated) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }
        var token = generateToken(user);
        return AuthenticationResponse.builder()
                .token(token)
                .authenticated(isAuthenticated)
                .build();
    }

    public String generateToken(User user) {
        try {
            JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(user.getUsername())
                    .issuer("SaleTicket")// nha phat hanh
                    .issueTime(new Date())
                    .expirationTime(new Date(Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS).toEpochMilli())) // đặt
                                                                                                                     // thời
                                                                                                                     // gian
                                                                                                                     // hết
                                                                                                                     // hạn
                                                                                                                     // của
                                                                                                                     // token
                                                                                                                     // là
                                                                                                                     // thời
                                                                                                                     // gian
                                                                                                                     // hiện
                                                                                                                     // tại
                                                                                                                     // cộng
                                                                                                                     // với
                                                                                                                     // thời
                                                                                                                     // gian
                                                                                                                     // hợp
                                                                                                                     // lệ
                                                                                                                     // (VALID_DURATION)
                                                                                                                     // được
                                                                                                                     // cấu
                                                                                                                     // hình
                                                                                                                     // trong
                                                                                                                     // file
                                                                                                                     // application.yaml
                    .jwtID(UUID.randomUUID().toString())
                    .claim("scope", buildScopeString(user)) // claim: thêm thông tin vai trò người dùng vào token
                    .build();

            Payload payload = new Payload(claimsSet.toJSONObject());
            JWSObject jwsObject = new JWSObject(header, payload);

            // Ký token
            jwsObject.sign(new MACSigner(SECRET_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION, "lỗi tạo jwt token");
        }
    }

    private String buildScopeString(User user) {
        // co tac dung tao chuoi chua cac vai tro cua nguoi dung
        StringJoiner joiner = new StringJoiner(" ");// tạo một chuỗi với dấu cách phân tách
        if (!CollectionUtils.isEmpty(user.getRoles())) {
            // user.getRoles().forEach(joiner::add);// thêm từng vai trò vào chuỗi với dấu
            // cách phân tách Joiner:: add là
            // method reference trong Java 8 trở lên, tương đương với việc gọi
            // joiner.add(role) trong vòng lặp
            user.getRoles().forEach(role -> {
                joiner.add("ROLE_" + role.getName()); // thêm tên vai trò vào chuỗi
            });
        }
        return joiner.toString();
    }

    private SignedJWT verifyToken(String token, boolean isRefresh) throws ParseException, JOSEException {
        JWSVerifier verifier = new MACVerifier(SECRET_KEY.getBytes()); // tạo bộ xác minh token với khóa bí mật
        SignedJWT signedJWT = SignedJWT.parse(token);// phân tích chuỗi token thành đối tượng SignedJWT
        Date expirationTime = (isRefresh)
                ? new Date(signedJWT.getJWTClaimsSet().getIssueTime().toInstant()
                        .plus(REFRESH_DURATION, ChronoUnit.SECONDS).toEpochMilli())
                : signedJWT.getJWTClaimsSet().getExpirationTime();

        var verified = signedJWT.verify(verifier); // xác minh token
        if (!verified || expirationTime.before(new Date())) {
            // kiểm tra token có hợp lệ và chưa hết hạn chưa
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        if (invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID())) {
            // kiểm tra token có bị thu hồi chưa,
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        return signedJWT;
    }

    public AuthenticationResponse refreshToken(RefreshRequest request) {
        try {
            var signedToken = verifyToken(request.getToken(), true); // xác minh token con hiệu lực
            var jid = signedToken.getJWTClaimsSet().getJWTID();
            var expiryTime = signedToken.getJWTClaimsSet().getExpirationTime();
            InvalidatedToken invalidatedToken = InvalidatedToken.builder()// tạo đối tượng InvalidatedToken
                    .id(jid)
                    .expiryTime(expiryTime)
                    .build();
            invalidatedTokenRepository.save(invalidatedToken); // lưu token cũ vào danh sách thu hồi
            var username = signedToken.getJWTClaimsSet().getSubject();// lấy tên người dùng từ token
            var user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
            var newToken = generateToken(user); // tạo token mới
            return AuthenticationResponse.builder()
                    .token(newToken)
                    .authenticated(true)
                    .build(); // trả về token mới

        } catch (ParseException | JOSEException e) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
    }

    // logout
    public ApiResponse<Void> logout(LogoutRequest request) {
        try {
            var signedToken = verifyToken(request.getToken(), true); // xác minh token

            String jwt_id = signedToken.getJWTClaimsSet().getJWTID();// lấy jwt_id từ token
            Date expirationTime = signedToken.getJWTClaimsSet().getExpirationTime();// lấy thời gian hết hạn từ token
            InvalidatedToken invalidatedToken = InvalidatedToken.builder()// tạo đối tượng InvalidatedToken
                    .id(jwt_id)
                    .expiryTime(expirationTime)
                    .build();
            invalidatedTokenRepository.save(invalidatedToken);
            // lưu token đã bị thu hồi vào cơ sở dữ liệu
            log.info("Token invalidated successfully");
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (JOSEException e) {
            e.printStackTrace();
        } catch (AppException e) {
            log.info("Token is invalid or expired, no need to invalidate again");
            // khi token không hợp lệ hoặc đã hết hạn, không cần phải thu hồi lại, chỉ cần
            // ghi log và trả về phản hồi thành công
        }

        return ApiResponse.<Void>builder().build();
    }
}