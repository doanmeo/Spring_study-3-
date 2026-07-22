package com.saleticket.exam1.repository;

import com.saleticket.exam1.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    /**
      ATOMIC UPDATE: chống Race Condition.
     Chỉ trừ vé khi số lượng vé còn lại (availableTickets) >= số vé muốn mua (quantity).
     @return Số lượng dòng (rows) bị ảnh hưởng. Nếu trả về 0 nghĩa là đã hết vé.
     */
    @Modifying
    @Transactional
    @Query("UPDATE Event e SET e.availableTickets = e.availableTickets - :quantity WHERE e.id = :eventId AND e.availableTickets >= :quantity")
    int decrementTickets(@Param("eventId") Long eventId, @Param("quantity") int quantity);

     // 1. Chuyển sang SOLD_OUT nếu hết vé
    @Modifying
    @Transactional
    @Query("UPDATE Event e SET e.status = 'SOLD_OUT' WHERE e.status = 'ONGOING' AND e.availableTickets <= 0")
    int updateStatusToSoldOut();
    
    // 2. Hàm hoàn trả vé (Restore) khi Hóa đơn bị hủy
    @Modifying
    @Transactional
    @Query("UPDATE Event e SET e.availableTickets = e.availableTickets + :quantity WHERE e.id = :eventId")
    void restoreTickets(@Param("eventId") Long eventId, @Param("quantity") int quantity);
}