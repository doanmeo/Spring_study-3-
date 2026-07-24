package com.saleticket.exam1.service;

import com.saleticket.exam1.dto.request.BookingRequest;
import com.saleticket.exam1.dto.response.BookingResponse;
import com.saleticket.exam1.enums.BookingStatus;
import com.saleticket.exam1.entity.Booking;
import com.saleticket.exam1.entity.Event;
import com.saleticket.exam1.entity.Ticket;
import com.saleticket.exam1.enums.TicketStatus;
import com.saleticket.exam1.entity.User;
import com.saleticket.exam1.exception.AppException;
import com.saleticket.exam1.exception.ErrorCode;
import com.saleticket.exam1.repository.BookingRepository;
import com.saleticket.exam1.repository.EventRepository;
import com.saleticket.exam1.repository.TicketRepository;
import com.saleticket.exam1.repository.UserRepository;

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
                .status(BookingStatus.PENDING.name()) // Sử dụng enum
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
                    .status(TicketStatus.RESERVED.name()) // Sử dụng enum
                    .build();
            tickets.add(ticket);
        }
        ticketRepository.saveAll(tickets);

        // Trả về ID của Booking để Frontend chuyển sang trang Thanh toán
        return "Đặt vé thành công! Mã đơn hàng: " + booking.getId() + ". Vui lòng thanh toán trong 15 phút!";
    }
     @Transactional
    public String payBooking(String bookingId) {
        // 1. Xác thực User hiện tại
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        
        // 2. Tìm Hóa đơn
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));
        // 3. Kiểm tra bảo mật: Chỉ chính chủ hóa đơn mới được thanh toán
        if (!booking.getUser().getUsername().equals(username)) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }

        // 4. Kiểm tra trạng thái: Phải là PENDING mới được thanh toán
        if (!booking.getStatus().equals("PENDING")) {
            throw new AppException(ErrorCode.PAYMENT_STATUS_INVALID);
        }

        // 5. Cập nhật trạng thái
        booking.setStatus(BookingStatus.COMPLETED.name());
        bookingRepository.save(booking);
        
        // Cập nhật tất cả vé bên trong thành PAID
        booking.getTickets().forEach(ticket -> ticket.setStatus(TicketStatus.PAID.name()));
        ticketRepository.saveAll(booking.getTickets());

        return "Thanh toán thành công hóa đơn " + bookingId + "!";
    }

    // Xem lịch sử đặt vé của bản thân
    public List<BookingResponse> getMyBookings() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        return bookingRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(booking -> new BookingResponse(
                        booking.getId(),
                        booking.getTickets().isEmpty() ? "Unknown Event" : booking.getTickets().get(0).getEvent().getName(),
                        booking.getTickets().size(),
                        booking.getTotalAmount(),
                        booking.getStatus(),
                        booking.getCreatedAt()
                )).toList();
    }

    // Tự tay hủy đơn (Chỉ hủy được khi PENDING)
    @Transactional
    public String cancelMyBooking(String bookingId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        // Chỉ chủ đơn mới được hủy
        if (!booking.getUser().getUsername().equals(username)) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }

        if (!booking.getStatus().equals("PENDING")) {
            throw new RuntimeException("Chỉ có thể hủy đơn hàng đang chờ thanh toán!");
        }

        // Đổi trạng thái Booking
        booking.setStatus("CANCELLED");
        bookingRepository.save(booking);

        // Hủy các vé bên trong
        booking.getTickets().forEach(ticket -> ticket.setStatus("CANCELLED"));
        
        // Nhả vé (Restore tickets) lại cho Event
        int quantity = booking.getTickets().size();
        if (quantity > 0) {
            Long eventId = booking.getTickets().get(0).getEvent().getId();
            eventRepository.restoreTickets(eventId, quantity);
        }

        return "Hủy đơn hàng thành công! Đã hoàn trả vé cho hệ thống.";
    }

}
