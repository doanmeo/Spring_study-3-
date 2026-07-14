package com.saleticket.exam1.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.oauth2.sdk.ParseException;
import com.saleticket.exam1.dto.request.AuthenticationRequest;
import com.saleticket.exam1.dto.request.LogoutRequest;
import com.saleticket.exam1.dto.request.RefreshRequest;
import com.saleticket.exam1.dto.response.ApiResponse;
import com.saleticket.exam1.dto.response.AuthenticationResponse;
import com.saleticket.exam1.service.AuthenticationService;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {
    AuthenticationService authenticationService;
    // controller -> service -> repository -> database
    // dto_request -> entity
    // request -> response
    // ApiResponse -> AuthenticationResponse: chứa thông tin trả về cho client

    @PostMapping("/login")
    ApiResponse<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        var isAuthenticated = authenticationService.authenticate(request);
        return ApiResponse.<AuthenticationResponse>builder() // builder() để tạo đối tượng ApiResponse
                .result(isAuthenticated)
                .build();

    }


    @PostMapping("/logout")
    ApiResponse<Void> logout(@RequestBody LogoutRequest request) throws ParseException, JOSEException {

        authenticationService.logout(request);
        return ApiResponse.<Void>builder()
                .build();
    }

    @PostMapping("/refresh") // public để tạo token mới khi token cũ hết hạn
    ApiResponse<AuthenticationResponse> refresh(@RequestBody RefreshRequest request)
            throws ParseException, JOSEException {

        var authResponse = authenticationService.refreshToken(request); 
        return ApiResponse.<AuthenticationResponse>builder()
                .result(authResponse)
                .build();
    }

}
