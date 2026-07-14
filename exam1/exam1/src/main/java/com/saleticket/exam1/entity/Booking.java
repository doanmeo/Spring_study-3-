package com.saleticket.exam1.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "bookings")
public class Booking extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    // Trạng thái hóa đơn: PENDING, COMPLETED, FAILED, CANCELLED
    @Column(nullable = false, length = 20)
    private String status;

    // Ánh xạ 1 Hóa đơn -> Nhiều Vé
    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Ticket> tickets;
}