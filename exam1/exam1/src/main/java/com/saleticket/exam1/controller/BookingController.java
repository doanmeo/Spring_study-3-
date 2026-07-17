package com.saleticket.exam1.controller;

import com.saleticket.exam1.dto.request.BookingRequest;
import com.saleticket.exam1.dto.response.ApiResponse;
import com.saleticket.exam1.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class BookingController {

    BookingService bookingService;

    // API Mua vé (Phải có Token mới mua được)
    @PostMapping
    public ApiResponse<String> bookTicket(@Valid @RequestBody BookingRequest request) {
        return ApiResponse.<String>builder()
                .code(200)
                .message("Thành công")
                .result(bookingService.bookTicket(request))
                .build();
    }
}