package com.saleticket.exam1.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record BookingRequest(
    @NotNull(message = "Vui lòng chọn sự kiện muốn mua vé")
    Long eventId,

    @NotNull(message = "Vui lòng nhập số lượng vé")
    @Min(value = 1, message = "Số lượng mua ít nhất là 1")
    @Max(value = 5, message = "Số lượng mua tối đa là 5")
    Integer quantity
) {}