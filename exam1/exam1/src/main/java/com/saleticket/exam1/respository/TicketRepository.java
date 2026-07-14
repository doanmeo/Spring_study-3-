package com.saleticket.exam1.respository;

import com.saleticket.exam1.entity.Event;
import com.saleticket.exam1.entity.Ticket;
import com.saleticket.exam1.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, String> {
    // Hàm này kết hợp với Unique Constraint ở DB tạo thành 2 lớp bảo vệ
    boolean existsByEventAndUser(Event event, User user);
    
}