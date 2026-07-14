package com.saleticket.exam1.dto.response;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // Chỉ parse các field không null ra JSON
public class ApiResponse<T> {
    private int code;
    private String message;
    private T result;
}