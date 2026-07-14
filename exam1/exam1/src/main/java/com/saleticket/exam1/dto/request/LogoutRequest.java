package com.saleticket.exam1.dto.request;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
// @NoArgsConstructor
@Builder
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class LogoutRequest {
    // token cần kiểm tra hợp lệ hay không
    
    String token;

}