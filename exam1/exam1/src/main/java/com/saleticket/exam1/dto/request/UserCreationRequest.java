package com.saleticket.exam1.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public record UserCreationRequest(
    @NotBlank(message = "Username không được để trống")
    @Size(min = 3, message = "Username phải có ít nhất 3 ký tự")
    String username,

    @NotBlank(message = "Password không được để trống")
    @Size(min = 6, message = "Password phải có ít nhất 6 ký tự")
    String password,

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    String email
) {}