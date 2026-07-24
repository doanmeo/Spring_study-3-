package com.saleticket.exam1.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BookingResponse(
    String id,
    String eventName,
    Integer quantity,
    BigDecimal totalAmount,
    String status,
    LocalDateTime createdAt
) {}
