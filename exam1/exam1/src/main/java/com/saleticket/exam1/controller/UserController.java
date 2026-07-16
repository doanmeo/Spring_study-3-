package com.saleticket.exam1.controller;

import com.saleticket.exam1.dto.request.UserCreationRequest;
import com.saleticket.exam1.dto.request.UserUpdateRequest;
import com.saleticket.exam1.dto.response.ApiResponse;
import com.saleticket.exam1.dto.response.UserResponse;
import com.saleticket.exam1.entity.User;
import com.saleticket.exam1.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // API 1: Lấy thông tin cá nhân của người đang đăng nhập
    @GetMapping("/me")
    ApiResponse<UserResponse> getMyProfile() {
        return ApiResponse.<UserResponse>builder()
                .code(200)
                .message("Lấy thông tin profile thành công!")
                .result(userService.getMyInfo())
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    ApiResponse<List<UserResponse>> getAllUsers() {
        return ApiResponse.<List<UserResponse>>builder()
                .code(200)
                .message("Lấy thông tin user thành công!")
                .result(userService.getAllUsers())
                .build();
    }

    // API 2: Đăng ký tài khoản
    @PostMapping("/register")
    public ApiResponse<UserResponse> register(@RequestBody @Valid UserCreationRequest request) {
        try {
            return ApiResponse.<UserResponse>builder()
                    .code(200)
                    .message("Đăng ký thành công!")
                    .result(userService.createUser(request))
                    .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PutMapping("/{userid}")
    public ApiResponse<UserResponse> updateUser(@RequestBody UserUpdateRequest request,
            @PathVariable("userid") String userid) {
        return ApiResponse.<UserResponse>builder()
                .code(200)
                .message("Cập nhật thông tin thành công!")
                .result(userService.updateUser(request, userid))
                .build();
    }

    // API 2: Test phân quyền (Chỉ dành cho ADMIN)
    // Lưu ý cấu hình trong Auth Service của bạn: scope là "ROLE_ADMIN".
    // Nên khi dùng hasRole('ADMIN'), Spring Security sẽ tự động kiểm tra xem có
    // chuỗi "ROLE_ADMIN" trong Context không.
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/dashboard")
    ApiResponse<String> getAdminDashboard() {
        return ApiResponse.<String>builder()
                .code(200)
                .message("Xác thực quyền Admin thành công!")
                .result("Đây là dữ liệu tuyệt mật chỉ Admin mới thấy.")
                .build();
    }
}
