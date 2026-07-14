package com.saleticket.exam1.dto.request;
import jakarta.validation.constraints.NotBlank;



public record AuthenticationRequest(
    @NotBlank(message = "Username không được để trống")
    String username,
    
    @NotBlank(message = "Password không được để trống")
    String password
) {}