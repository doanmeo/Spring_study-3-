package com.saleticket.exam1.config;

import javax.print.attribute.standard.Media;

import org.apache.tomcat.util.http.parser.MediaType;
import org.springframework.security.core.AuthenticationException;

import com.saleticket.exam1.dto.response.ApiResponse;
import com.saleticket.exam1.exception.ErrorCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

public class MyCustomAuthenticationEntryPoint implements org.springframework.security.web.AuthenticationEntryPoint{
    @Override
    // Khi người dùng không có quyền truy cập vào tài nguyên, phương thức commence sẽ được gọi để trả về lỗi dưới dạng JSON thay vì trang login mặc định của Spring Security
    public void commence(HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException)
            throws java.io.IOException,ServletException {
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;
        response.setStatus(errorCode.getStatusCode().value());//401
        response.setContentType(org.springframework.http.MediaType.APPLICATION_JSON_VALUE);// định dạng trả về là json
        ApiResponse<?>  apiResponse = ApiResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();
        ObjectMapper objectMapper = new ObjectMapper(); // chuyển đổi đối tượng Java thành JSON
    response.getWriter().write(
            objectMapper.writeValueAsString(apiResponse)
        );
        response.flushBuffer();// đẩy dữ liệu trong bộ đệm ra client
       
    }
}
