package com.saleticket.exam1.service;

import com.saleticket.exam1.dto.request.EventCreationRequest;
import com.saleticket.exam1.dto.response.EventResponse;
import com.saleticket.exam1.enums.EventStatus;
import com.saleticket.exam1.entity.Event;
import com.saleticket.exam1.mapper.EventMapper;
import com.saleticket.exam1.repository.EventRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    // Admin tạo sự kiện
    public EventResponse createEvent(EventCreationRequest request) {
        Event event = eventMapper.toEvent(request);
        event.setStatus(EventStatus.UPCOMING.name()); // Trạng thái mặc định khi mới tạo

        event = eventRepository.save(event);
        return eventMapper.toEventResponse(event);
    }

    // Lấy danh sách sự kiện (Có thể mở rộng thêm Phân trang - Pageable sau)
    public List<EventResponse> getAllEvents() {
        return eventRepository.findAll().stream()
                .map(eventMapper::toEventResponse)
                .toList();
    }

}
