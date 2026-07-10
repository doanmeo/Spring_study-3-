package com.saleticket.exam1.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "events")
public class Event extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;

    // Tổng số vé ban đầu
    @Column(name = "total_tickets", nullable = false)
    private Integer totalTickets;

    // Số vé còn lại 
    @Column(name = "available_tickets", nullable = false)
    private Integer availableTickets;

    @Column(nullable = false)
    private BigDecimal price;

    // Trạng thái sự kiện: UPCOMING, ONGOING, COMPLETED, CANCELLED
    @Column(nullable = false, length = 20)
    private String status; 
}
