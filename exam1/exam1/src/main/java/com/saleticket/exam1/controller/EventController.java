package com.saleticket.exam1.controller;

import com.saleticket.exam1.dto.request.EventCreationRequest;
import com.saleticket.exam1.dto.response.ApiResponse;
import com.saleticket.exam1.dto.response.EventResponse;
import com.saleticket.exam1.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

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
    public ApiResponse<List<EventResponse>> getAllEvents() {
        return ApiResponse.<List<EventResponse>>builder()
                .code(200)
                .result(eventService.getAllEvents())
                .build();
    }

    // Thêm sự kiện mới (Chỉ ADMIN)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<EventResponse> createEvent(@Valid @RequestBody EventCreationRequest request) {
        return ApiResponse.<EventResponse>builder()
                .code(200)
                .message("Tạo sự kiện thành công!")
                .result(eventService.createEvent(request))
                .build();
    }
}