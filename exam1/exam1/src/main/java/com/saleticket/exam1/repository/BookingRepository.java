package com.saleticket.exam1.repository;

import com.saleticket.exam1.entity.Booking;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends JpaRepository<Booking, String> {
    // Tìm các Booking đang PENDING và đã quá hạn (Ví dụ: tạo trước thời điểm 15 phút trước)
    @Query("SELECT b FROM Booking b WHERE b.status = 'PENDING' AND b.createdAt <= :expireTime")
    List<Booking> findExpiredBookings(@Param("expireTime") LocalDateTime expireTime);
}