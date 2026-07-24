package com.saleticket.exam1.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EventUpdateRequest(
                @NotBlank(message = "Tên sự kiện không được để trống") String name,
                String description,
                @NotNull(message = "Ngày sự kiện không được để trống") @Future(message = "INVALID_EVENT_DATE") 
                LocalDateTime eventDate,
                @Min(value = 1, message = "Tổng số vé phải lớn hơn 0") Integer totalTickets,
                @Min(value = 0, message = "Giá vé không hợp lệ") BigDecimal price) {
}