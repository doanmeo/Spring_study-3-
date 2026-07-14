package com.saleticket.exam1.dto.response;

import lombok.Builder;

@Builder
public record AuthenticationResponse(
    String token, 
    boolean authenticated
) {}
