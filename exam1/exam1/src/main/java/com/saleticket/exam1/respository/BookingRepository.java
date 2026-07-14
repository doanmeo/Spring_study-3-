package com.saleticket.exam1.respository;

import com.saleticket.exam1.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends JpaRepository<Booking, String> {
}