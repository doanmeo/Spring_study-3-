package com.saleticket.exam1.controller;

import com.saleticket.exam1.dto.request.EventCreationRequest;
import com.saleticket.exam1.dto.request.EventUpdateRequest;
import com.saleticket.exam1.dto.response.ApiResponse;
import com.saleticket.exam1.dto.response.EventResponse;
import com.saleticket.exam1.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class EventController {
    
    EventService eventService;

    // Xem danh sách sự kiện (Ai cũng xem được, public)
    @GetMapping
    public ApiResponse<Page<EventResponse>> getAllEvents(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        // Chú ý: Frontend thường đếm page từ 1, nhưng Spring Boot đếm page từ 0
        int pageNumber = page > 0 ? page - 1 : 0;
        
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNumber, size, sort);

        return ApiResponse.<Page<EventResponse>>builder()
                .code(200)
                .result(eventService.getAllEvents(pageable))
                .build();
    }
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ApiResponse<EventResponse> createEvent(@Valid @RequestBody EventCreationRequest request) {
        return ApiResponse.<EventResponse>builder()
                .code(200)
                .message("Tạo sự kiện thành công!")
                .result(eventService.createEvent(request))
                .build();
    }
    // Xem chi tiết (Public)
    @GetMapping("/{id}")
    public ApiResponse<EventResponse> getEventDetail(@PathVariable Long id) {
        return ApiResponse.<EventResponse>builder()
                .result(eventService.getEventById(id))
                .build();
    }

    // Cập nhật sự kiện 
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ApiResponse<EventResponse> updateEvent(@PathVariable Long id, @Valid @RequestBody EventUpdateRequest request) {
        return ApiResponse.<EventResponse>builder()
                .result(eventService.updateEvent(id, request))
                .build();
    }

    // Hủy sự kiện 
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ApiResponse<String> cancelEvent(@PathVariable Long id) {
        eventService.cancelEvent(id);
        return ApiResponse.<String>builder()
                .message("Đã hủy sự kiện!")
                .build();
    }
}