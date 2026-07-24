package com.saleticket.exam1.service;

import com.saleticket.exam1.dto.request.BookingRequest;
import com.saleticket.exam1.dto.response.BookingResponse;
import com.saleticket.exam1.entity.Booking;
import com.saleticket.exam1.entity.Event;
import com.saleticket.exam1.entity.Ticket;
import com.saleticket.exam1.entity.User;
import com.saleticket.exam1.exception.AppException;
import com.saleticket.exam1.exception.ErrorCode;
import com.saleticket.exam1.repository.BookingRepository;
import com.saleticket.exam1.repository.EventRepository;
import com.saleticket.exam1.repository.TicketRepository;
import com.saleticket.exam1.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @InjectMocks
    private BookingService bookingService;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private BookingRepository bookingRepository;

    private User currentUser;

    @BeforeEach
    void setUp() {
        currentUser = new User();
        currentUser.setUsername("user1");
        currentUser.setEmail("user1@example.com");
        currentUser.setPassword("encoded");
        currentUser.setRoles(Set.of());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication authentication = org.mockito.Mockito.mock(Authentication.class);
        org.mockito.BDDMockito.given(authentication.getName()).willReturn("user1");
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("bookTicket - khi số vé đặt vượt availableTickets thì ném SOLD_OUT")
    void bookTicket_WhenQuantityExceedsAvailableTickets_ShouldThrowSoldOut() {
        BookingRequest request = new BookingRequest(1L, 3);
        Event event = Event.builder()
                .id(1L)
                .name("Concert")
                .description("desc")
                .eventDate(LocalDateTime.now().plusDays(1))
                .totalTickets(8)
                .availableTickets(2)
                .price(BigDecimal.valueOf(10))
                .status("UPCOMING")
                .build();

        given(userRepository.findByUsername("user1")).willReturn(java.util.Optional.of(currentUser));
        given(eventRepository.findById(1L)).willReturn(java.util.Optional.of(event));
        given(ticketRepository.countByEventAndUser(event, currentUser)).willReturn(0L);
        given(eventRepository.decrementTickets(1L, 3)).willReturn(0);

        AppException exception = assertThrows(AppException.class, () -> bookingService.bookTicket(request));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.SOLD_OUT);
    }

    @Test
    @DisplayName("bookTicket - khi đủ vé thì tạo booking và lưu ticket")
    void bookTicket_WhenValid_ShouldCreateBookingAndTickets() {
        BookingRequest request = new BookingRequest(1L, 2);
        Event event = Event.builder()
                .id(1L)
                .name("Concert")
                .description("desc")
                .eventDate(LocalDateTime.now().plusDays(1))
                .totalTickets(8)
                .availableTickets(8)
                .price(BigDecimal.valueOf(6))
                .status("UPCOMING")
                .build();

        Booking booking = Booking.builder()
                .id("booking-1")
                .user(currentUser)
                .totalAmount(BigDecimal.valueOf(12))
                .status("PENDING")
                .build();

        given(userRepository.findByUsername("user1")).willReturn(java.util.Optional.of(currentUser));
        given(eventRepository.findById(1L)).willReturn(java.util.Optional.of(event));
        given(ticketRepository.countByEventAndUser(event, currentUser)).willReturn(0L);
        given(eventRepository.decrementTickets(1L, 2)).willReturn(1);
        given(bookingRepository.save(any(Booking.class))).willReturn(booking);
        given(ticketRepository.saveAll(anyList())).willReturn(List.of());

        String result = bookingService.bookTicket(request);

        assertThat(result).contains("booking-1");
        verify(bookingRepository).save(any(Booking.class));
        verify(ticketRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("payBooking - khi booking pending và user là chủ booking thì cập nhật status thành COMPLETED")
    void payBooking_WhenPendingAndOwnerMatches_ShouldMarkBookingCompleted() {
        Booking booking = Booking.builder()
                .id("booking-1")
                .user(currentUser)
                .status("PENDING")
                .tickets(List.of(Ticket.builder().status("RESERVED").build()))
                .build();

        given(bookingRepository.findById("booking-1")).willReturn(java.util.Optional.of(booking));
        given(ticketRepository.saveAll(anyList())).willReturn(List.of());
        given(bookingRepository.save(any(Booking.class))).willReturn(booking);

        String result = bookingService.payBooking("booking-1");

        assertThat(result).contains("Thanh toán thành công");
        assertThat(booking.getStatus()).isEqualTo("COMPLETED");
        verify(bookingRepository).save(any(Booking.class));
        verify(ticketRepository).saveAll(anyList());
    }

}
