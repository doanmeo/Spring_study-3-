package com.saleticket.exam1.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EventResponse(
    Long id,
    String name,
    String description,
    LocalDateTime eventDate,
    Integer totalTickets,
    Integer availableTickets,
    BigDecimal price,
    String status
) {}