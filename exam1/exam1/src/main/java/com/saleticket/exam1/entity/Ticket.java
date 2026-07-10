package com.saleticket.exam1.entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
// BẪY DATABASE: 1 User chỉ được sở hữu tối đa 1 vé cho 1 Event cụ thể
@Table(name = "tickets", uniqueConstraints = {
    @UniqueConstraint(name = "uk_event_user", columnNames = {"event_id", "user_id"})
})
public class Ticket extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    // Liên kết với hóa đơn mua hàng
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking;

    // Mã QR dùng để check-in tại cổng sự kiện
    @Column(name = "qr_code_data", unique = true, nullable = false)
    private String qrCodeData; 

    // RESERVED, PAID, CHECKED_IN, CANCELLED
    @Column(nullable = false, length = 20)
    private String status; 
}