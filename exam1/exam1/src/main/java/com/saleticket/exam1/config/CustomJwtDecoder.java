package com.saleticket.exam1.config;

import com.saleticket.exam1.repository.InvalidatedTokenRepository;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

public class CustomJwtDecoder implements JwtDecoder {

    private final NimbusJwtDecoder nimbusJwtDecoder;
    private final InvalidatedTokenRepository invalidatedTokenRepository;

    public CustomJwtDecoder(NimbusJwtDecoder nimbusJwtDecoder, InvalidatedTokenRepository invalidatedTokenRepository) {
        this.nimbusJwtDecoder = nimbusJwtDecoder;
        this.invalidatedTokenRepository = invalidatedTokenRepository;
    }

    @Override
    public Jwt decode(String token) throws JwtException {
        // 1. Giải mã token như bình thường
        Jwt jwt = nimbusJwtDecoder.decode(token);

        // 2. Kiểm tra xem token có trong danh sách bị thu hồi không
        if (invalidatedTokenRepository.existsById(jwt.getId())) {
            // Nếu có, ném lỗi để báo token không hợp lệ
            throw new JwtException("Token has been invalidated");
        }

        // 3. Nếu không, trả về token đã giải mã
        return jwt;
    }
}
