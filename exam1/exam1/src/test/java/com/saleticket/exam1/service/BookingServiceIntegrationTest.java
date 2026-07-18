package com.saleticket.exam1.service;

import com.saleticket.exam1.dto.request.BookingRequest;
import com.saleticket.exam1.entity.Event;
import com.saleticket.exam1.entity.Role;
import com.saleticket.exam1.entity.User;
import com.saleticket.exam1.repository.BookingRepository;
import com.saleticket.exam1.repository.EventRepository;
import com.saleticket.exam1.repository.RoleRepository;
import com.saleticket.exam1.repository.TicketRepository;
import com.saleticket.exam1.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource("/test.properties") // Sử dụng H2 Database ảo
class BookingServiceIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private Event testEvent;
    private List<User> testUsers = new ArrayList<>();

    @BeforeEach
    void setUp() {
        // Ghi dữ liệu thật vào H2 DB để chuẩn bị test
        
        // 1. Tạo sự kiện với ĐÚNG 10 VÉ
        testEvent = Event.builder()
                .name("BlackPink Concert")
                .eventDate(LocalDateTime.now().plusDays(10))
                .totalTickets(10)
                .availableTickets(10)
                .price(BigDecimal.valueOf(1000000))
                .status("ONGOING")
                .build();
        testEvent = eventRepository.save(testEvent);

        // 2. Tạo 100 User khác nhau để giả lập 100 người mua vé
        Role userRole = roleRepository.save(Role.builder().name("USER").build());
        for (int i = 0; i < 100; i++) {
            User user = User.builder()
                    .username("user" + i)
                    .password("pass" + i)
                    .email("user" + i + "@gmail.com")
                    .roles(Set.of(userRole))
                    .build();
            testUsers.add(userRepository.save(user));
        }
    }

    @AfterEach
    void tearDown() {
       // Đảm bảo tính Isolation (Cô lập): Xóa sạch dữ liệu sau mỗi hàm Test
        // PHẢI XÓA BẢNG CON TRƯỚC (Bảng có chứa Khóa ngoại - Foreign Key)
        ticketRepository.deleteAll();
        bookingRepository.deleteAll();
        
        // SAU ĐÓ MỚI XÓA BẢNG CHA
        eventRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
    }

    @Test
    @DisplayName("Khi 100 người cùng mua 10 vé, Hệ thống chỉ bán đúng 10 vé và báo lỗi cho 90 người còn lại")
    void testRaceCondition_When100UsersBuy10Tickets_ShouldOnlySell10Tickets() throws InterruptedException {
        // GIVEN: Chuẩn bị môi trường Multithreading
        int numberOfThreads = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(1);
        
        AtomicInteger successfulBookings = new AtomicInteger(0);
        AtomicInteger failedBookings = new AtomicInteger(0);

        // WHEN: 100 User lao vào gọi API bookTicket cùng lúc
        for (int i = 0; i < numberOfThreads; i++) {
            final User user = testUsers.get(i);
            executorService.submit(() -> {
                try {
                    // Giả lập User này đang đăng nhập trong Thread hiện tại
                    SecurityContextHolder.getContext().setAuthentication(
                            new UsernamePasswordAuthenticationToken(user.getUsername(), null, null)
                    );

                    latch.await(); // Tất cả các luồng đứng chờ ở đây

                    // Mỗi người mua 1 vé
                    BookingRequest request = new BookingRequest(testEvent.getId(), 1);
                    bookingService.bookTicket(request);
                    
                    successfulBookings.incrementAndGet();
                } catch (Exception e) {
                    failedBookings.incrementAndGet(); // Hết vé hoặc lỗi DB
                } finally {
                    SecurityContextHolder.clearContext();
                }
            });
        }

        // Kéo còi xuất phát! Giải phóng Latch để 100 luồng đồng loạt chạy
        latch.countDown();
        
        // Đợi tất cả chạy xong
        executorService.shutdown();
        while (!executorService.isTerminated()) {
            Thread.sleep(100);
        }

        // THEN: Kiểm chứng kết quả cuối cùng trong Database
        Event updatedEvent = eventRepository.findById(testEvent.getId()).orElseThrow();

        // 1. Chỉ 10 người mua thành công
        assertThat(successfulBookings.get()).isEqualTo(10);
        
        // 2. 90 người bị văng lỗi (AppException SOLD_OUT)
        assertThat(failedBookings.get()).isEqualTo(90);
        
        // 3. Số vé còn lại trong Database = 0
        assertThat(updatedEvent.getAvailableTickets()).isZero();
    }
}