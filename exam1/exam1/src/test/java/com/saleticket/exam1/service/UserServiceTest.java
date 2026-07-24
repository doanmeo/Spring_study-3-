package com.saleticket.exam1.service;

import com.saleticket.exam1.dto.request.UserCreationRequest;
import com.saleticket.exam1.dto.response.AuthenticationResponse;
import com.saleticket.exam1.dto.response.UserResponse;
import com.saleticket.exam1.entity.Role;
import com.saleticket.exam1.entity.User;
import com.saleticket.exam1.exception.AppException;
import com.saleticket.exam1.exception.ErrorCode;
import com.saleticket.exam1.mapper.UserMapper;
import com.saleticket.exam1.repository.RoleRepository;
import com.saleticket.exam1.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

// Dùng MockitoExtension thay vì @SpringBootTest để cô lập 100% (Không load file Config ngầm)
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    // @InjectMocks sẽ tự động tìm các @Mock bên dưới và "bơm" vào UserService
    @InjectMocks
    private UserService userService;

    // Dùng @Mock (của Mockito) thay vì @MockitoBean (của Spring) vì ta không cần
    // Spring Context nữa
    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    private UserCreationRequest request;
    private UserResponse expectedResponse;
    private User currentUser;
    private AuthenticationResponse authResponse;

    @BeforeEach
    void setUp() {
        request = new UserCreationRequest("doanmeo", "password123", "doan@gmail.com");
        expectedResponse = new UserResponse("id1", "doanmeo", "doan@gmail.com", Set.of("USER"));
        authResponse = new AuthenticationResponse("123", true);

        currentUser = new User();
        currentUser.setId("user-1");
        currentUser.setUsername("doanmeo");
        currentUser.setEmail("doan@gmail.com");
        currentUser.setPassword("encoded_password");
        currentUser.setRoles(new HashSet<>(Set.of(new Role(1L, "USER", "User role"))));

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication authentication = org.mockito.Mockito.mock(Authentication.class);
        org.mockito.BDDMockito.given(authentication.getName()).willReturn("doanmeo");
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }

    @Test
    @DisplayName("Tạo User thành công khi Username chưa tồn tại")
    void createUser_ValidRequest_ShouldReturnUserResponse() {
        // GIVEN: Giả lập Database trả về false (Chưa có ai dùng username này)
        // given(userRepository.existsByUsername(anyString())).willReturn(false);
        // Giả lập lấy Role USER
        given(roleRepository.findByName("USER")).willReturn(Optional.of(new Role(1L, "USER", "User role")));

        given(userMapper.toUser(any(UserCreationRequest.class))).willReturn(new User());

        // Giả lập mã hóa mật khẩu
        given(passwordEncoder.encode(anyString())).willReturn("encoded_password");

        // Giả lập lưu User và map ra Response
        given(userRepository.save(any(User.class))).willReturn(new User());
        given(userMapper.toUserResponse(any(User.class))).willReturn(expectedResponse);

        // WHEN: Thực thi hàm tạo User
        UserResponse actualResponse = userService.createUser(request);

        // THEN: Kiểm chứng kết quả
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.username()).isEqualTo("doanmeo");

        // Bây giờ verify sẽ thành công vì ApplicationInitConfig không còn chạy ngầm nữa
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Ném lỗi AppException khi Username đã bị trùng")
    void createUser_ExistedUsername_ShouldThrowAppException() {
        // GIVEN: Giả lập Database báo đã có người dùng username này rồi
        // The current implementation throws a DataIntegrityViolationException on save
        given(userMapper.toUser(any(UserCreationRequest.class))).willReturn(new User());
        given(passwordEncoder.encode(anyString())).willReturn("encoded_password");
        given(roleRepository.findByName("USER")).willReturn(Optional.of(new Role()));
        given(userRepository.save(any(User.class))).willThrow(new DataIntegrityViolationException("..."));

        // WHEN & THEN: Gọi hàm và bắt lỗi
        AppException exception = assertThrows(AppException.class, () -> userService.createUser(request));
        // Kiểm tra đúng mã lỗi
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_EXISTED);
    }

    @Test
    @DisplayName("upgradeToOrganizer - khi user tồn tại và role ORGANIZER đã được khởi tạo thì bổ sung role mới")
    void upgradeToOrganizer_WhenUserExists_ShouldAddOrganizerRole() {
        User savedUser = new User();
        savedUser.setId("user-1");
        savedUser.setUsername("doanmeo");
        savedUser.setEmail("doan@gmail.com");
        savedUser.setRoles(new HashSet<>(Set.of(new Role(1L, "USER", "User role"), new Role(2L, "ORGANIZER", "Organizer role"))));

        given(userRepository.findByUsername("doanmeo")).willReturn(Optional.of(currentUser));
        given(roleRepository.findByName("ORGANIZER")).willReturn(Optional.of(new Role(2L, "ORGANIZER", "Organizer role")));
        given(userRepository.save(any(User.class))).willReturn(savedUser);
        given(userMapper.toUserResponse(any(User.class))).willReturn(new UserResponse("user-1", "doanmeo", "doan@gmail.com", Set.of("USER", "ORGANIZER")));

        authResponse = userService.upgradeToOrganizer();

        assertThat(authResponse.token()).isNotNull();
        assertThat(authResponse.authenticated()).isTrue();
        verify(userRepository).save(any(User.class));
    }
}