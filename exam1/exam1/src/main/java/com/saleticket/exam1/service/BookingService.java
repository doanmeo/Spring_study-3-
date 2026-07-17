package com.saleticket.exam1.service;

import com.saleticket.exam1.dto.request.BookingRequest;
import com.saleticket.exam1.enums.BookingStatus;
import com.saleticket.exam1.entity.Booking;
import com.saleticket.exam1.entity.Event;
import com.saleticket.exam1.entity.Ticket;
import com.saleticket.exam1.enums.TicketStatus;
import com.saleticket.exam1.entity.User;
import com.saleticket.exam1.exception.AppException;
import com.saleticket.exam1.exception.ErrorCode;
import com.saleticket.exam1.respository.BookingRepository;
import com.saleticket.exam1.respository.EventRepository;
import com.saleticket.exam1.respository.TicketRepository;
import com.saleticket.exam1.respository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class BookingService {

    EventRepository eventRepository;
    UserRepository userRepository;
    TicketRepository ticketRepository;
    BookingRepository bookingRepository;

    @Transactional // BẮT BUỘC CÓ: Đảm bảo toàn vẹn dữ liệu
    public String bookTicket(BookingRequest request) {
        // 1. Lấy thông tin User đang đăng nhập
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // 2. Tìm sự kiện
        Event event = eventRepository.findById(request.eventId())
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        // 3. CHỐNG ĐẦU CƠ: Kiểm tra tổng số vé user đã mua cho sự kiện này (Giới hạn 5
        // vé)
        long currentTicketCount = ticketRepository.countByEventAndUser(event, user);
        if (currentTicketCount + request.quantity() > 5) {
            throw new AppException(ErrorCode.TICKET_LIMIT_EXCEEDED);
        }

        // 4. CHỐNG RACE CONDITION (BÁN ÂM VÉ): Atomic Update
        // Trừ đi số lượng vé tương ứng với request.quantity()
        int updatedRows = eventRepository.decrementTickets(event.getId(), request.quantity());

        if (updatedRows == 0) {
            // Nếu không cập nhật được (availableTickets < quantity), nghĩa là không đủ vé
            throw new AppException(ErrorCode.SOLD_OUT);
        }

        // 5. Nếu trừ vé thành công, tạo Hóa đơn (Booking)
        BigDecimal totalAmount = event.getPrice().multiply(BigDecimal.valueOf(request.quantity()));
        Booking booking = Booking.builder()
                .user(user)
                .totalAmount(totalAmount)
                .status(BookingStatus.COMPLETED.name()) // Sử dụng enum
                .build();
        booking = bookingRepository.save(booking);

        // 6. Tạo danh sách Vé (Ticket) và sinh mã QR cho từng vé
        List<Ticket> tickets = new ArrayList<>();
        for (int i = 0; i < request.quantity(); i++) {
            Ticket ticket = Ticket.builder()
                    .event(event)
                    .user(user)
                    .booking(booking)
                    .qrCodeData(UUID.randomUUID().toString()) // Sinh mã QR ngẫu nhiên cho mỗi vé
                    .status(TicketStatus.PAID.name()) // Sử dụng enum
                    .build();
            tickets.add(ticket);
        }
        ticketRepository.saveAll(tickets);

        return "Chúc mừng bạn đã giành được " + request.quantity() + " vé thành công!";
    }
}
