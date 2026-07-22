package com.saleticket.exam1.service;

import com.saleticket.exam1.entity.Booking;
import com.saleticket.exam1.repository.BookingRepository;
import com.saleticket.exam1.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class SystemSchedulerService {

    EventRepository eventRepository;
    BookingRepository bookingRepository;

    // Cơ chế HOLD VÉ: Quét các hóa đơn chưa thanh toán mỗi 1 phút
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void releaseExpiredTickets() {
        // Lấy thời điểm 15 phút trước. Các hóa đơn tạo trước thời điểm này sẽ bị coi là hết hạn.
        LocalDateTime expireTime = LocalDateTime.now().minusMinutes(15);
        List<Booking> expiredBookings = bookingRepository.findExpiredBookings(expireTime);

        if (expiredBookings.isEmpty()) return;

        for (Booking booking : expiredBookings) {
            // 1. Cập nhật trạng thái Hóa đơn
            booking.setStatus("EXPIRED");
            bookingRepository.save(booking);

            // 2. Tính xem hóa đơn này giữ bao nhiêu vé
            int heldTickets = booking.getTickets().size();

            // 3. Hủy trạng thái các vé bên trong (Từ PAID/RESERVED -> CANCELLED)
            booking.getTickets().forEach(ticket -> ticket.setStatus("CANCELLED"));

            // 4. trả vé lại cho Sự kiện (Hoàn trả vào availableTickets)
            if (heldTickets > 0) {
                 Long eventId = booking.getTickets().get(0).getEvent().getId();
                 eventRepository.restoreTickets(eventId, heldTickets);
            }
            
            log.info("Đã hủy Hóa đơn ID: {} và hoàn trả {} vé cho Sự kiện", booking.getId(), heldTickets);
        }
    }

    // Quét sự kiện hết vé mỗi 1 phút
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void checkAndMarkSoldOutEvents() {
        int soldOut = eventRepository.updateStatusToSoldOut();
        if (soldOut > 0) {
            log.info("Có {} sự kiện vừa tự động cập nhật trạng thái CHÁY VÉ (SOLD_OUT)", soldOut);
        }
    }
}