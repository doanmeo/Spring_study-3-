package com.saleticket.exam1.service;

import com.saleticket.exam1.dto.request.EventCreationRequest;
import com.saleticket.exam1.dto.request.EventUpdateRequest;
import com.saleticket.exam1.dto.response.EventResponse;
import com.saleticket.exam1.enums.EventStatus;
import com.saleticket.exam1.exception.AppException;
import com.saleticket.exam1.exception.ErrorCode;
import com.saleticket.exam1.entity.Event;
import com.saleticket.exam1.entity.User;
import com.saleticket.exam1.mapper.EventMapper;
import com.saleticket.exam1.repository.EventRepository;
import com.saleticket.exam1.repository.UserRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EventMapper eventMapper;

    public EventResponse createEvent(EventCreationRequest request) {
        
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User organizer = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Event event = Event.builder()
                .name(request.name())
                .description(request.description())
                .eventDate(request.eventDate())
                .totalTickets(request.totalTickets())
                .availableTickets(request.totalTickets())
                .price(request.price())
                .status("UPCOMING")
                .organizer(organizer) // Set quyền sở hữu
                .build();
                
        return eventMapper.toEventResponse(eventRepository.save(event));
    }
    // Lấy danh sách sự kiện (Có thể mở rộng thêm Phân trang - Pageable sau)
   public Page<EventResponse> getAllEvents(Pageable pageable) {
        return eventRepository.findAll(pageable)
                .map(eventMapper::toEventResponse);
    }

      // 1. Lấy chi tiết 1 sự kiện
    public EventResponse getEventById(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));
        return eventMapper.toEventResponse(event);
    }

    public EventResponse updateEvent(Long id, EventUpdateRequest request) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        verifyOwnershipOrAdmin(event); // Check quyền

        int ticketsSold = event.getTotalTickets() - event.getAvailableTickets();
        
        //không được giảm thấp hơn số đã bán!
        if (request.totalTickets() < ticketsSold) {
            throw new AppException(ErrorCode.EVENT_INVALID_TICKET_COUNT);
        }

        int delta = request.totalTickets() - event.getTotalTickets();
        
        eventMapper.updateEvent(request, event);

        event.setAvailableTickets(event.getAvailableTickets() + delta); // Cập nhật vé khả dụng an toàn
        
        return eventMapper.toEventResponse(eventRepository.save(event));
    }

    // 3. Hủy sự kiện
    public void cancelEvent(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));
        
        verifyOwnershipOrAdmin(event); // Check quyền
        
        event.setStatus("CANCELLED");
        eventRepository.save(event);
        
        // Luồng background đi Refund tiền sẽ được tích hợp bằng Kafka/RabbitMQ ở Phase sau
    }


    // HÀM TIỆN ÍCH KIỂM TRA QUYỀN SỞ HỮU
    private void verifyOwnershipOrAdmin(Event event) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN"));

        // Nếu không phải ADMIN, và cũng không phải Chủ sự kiện này -> Ném lỗi 403
        if (!isAdmin && !event.getOrganizer().getUsername().equals(currentUsername)) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }
    }

}
