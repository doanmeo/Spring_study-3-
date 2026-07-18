package com.saleticket.exam1.service;

import com.saleticket.exam1.dto.request.UserCreationRequest;
import com.saleticket.exam1.dto.request.UserUpdateRequest;
import com.saleticket.exam1.dto.response.UserResponse;
import com.saleticket.exam1.entity.Role;

import com.saleticket.exam1.entity.User;
import com.saleticket.exam1.exception.AppException;
import com.saleticket.exam1.exception.ErrorCode;
import com.saleticket.exam1.mapper.UserMapper;
import com.saleticket.exam1.repository.RoleRepository;
import com.saleticket.exam1.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class UserService {

    UserRepository userRepository;
    RoleRepository roleRepository;

    UserMapper userMapper;
    // PasswordEncoder encoder = new BCryptPasswordEncoder(10);// mã hóa mật khẩu với độ dài 10 ký tự
    PasswordEncoder passwordEncoder;

    public UserResponse getMyInfo() {
        // 1. Lấy thông tin xác thực từ Security Context
        // Trong OAuth2 Resource Server, Name mặc định chính là claim "sub" (subject)
        // của JWT
        var context = SecurityContextHolder.getContext();
        String username = context.getAuthentication().getName();

        // 2. Chọc xuống DB lấy thông tin User
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // 3. Trả về DTO (Dùng Record siêu gọn của Java 25)
        return userMapper.toUserResponse(user);// Chuyển đổi thực thể User thành UserResponse và trả về kết quả

    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toUserResponse)
                .toList();
    }

    public UserResponse createUser(UserCreationRequest request) {

        // if (userRepository.existsByUsername(request.getUsername())) {
        // throw new AppException(ErrorCode.User_Existed);
        // } ko can nua vi da co Unique trong entity
        User user = userMapper.toUser(request);

        user.setPassword(passwordEncoder.encode(request.password()));

        // user.setUsername(request.getUsername());
        // user.setPassword(request.getPassword());
        // user.setEmail(request.getEmail());
        // user.setFirstName(request.getFirstName());
        // user.setLastName(request.getLastName());
        // user.setDob(request.getDob());

        HashSet<Role> roles = new HashSet<>();
        roleRepository.findByName(com.saleticket.exam1.enums.Role.USER.name()).ifPresent(roles::add);
        user.setRoles(roles); // Gán vai trò USER mặc định

        try {
            user = userRepository.save(user); // Lưu người dùng vào cơ sở dữ liệu
        } catch (DataIntegrityViolationException e) {
            throw new AppException(ErrorCode.USER_EXISTED); // Ném ngoại lệ nếu người dùng đã tồn tại
        }

        return userMapper.toUserResponse(user); // Chuyển đổi thực thể User thành UserResponse và trả về kết quả
    }

    public UserResponse updateUser(UserUpdateRequest request, String userid) {
        User user = userRepository.findById(userid)
                .orElseThrow(() -> new RuntimeException("User not found"));
        // user.setPassword(request.getPassword());
        // user.setEmail(request.getEmail());
        // user.setFirstName(request.getFirstName());
        // user.setLastName(request.getLastName());
        // user.setDob(request.getDob());
        // Kiểm tra xem email mới có bị trùng với người khác không
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.USER_EXISTED, "Email đã được sử dụng!");
        }
        userMapper.updateUser(request, user);

        user.setPassword(passwordEncoder.encode(request.getPassword()));

        var roles = new HashSet<Role>();
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            Set<Role> foundRoles = roleRepository.findByNameIn(request.getRoles());
            if (foundRoles.size() != request.getRoles().size()) {
                throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
            }
            roles.addAll(foundRoles);
        }

        user.setRoles(roles);
        return userMapper.toUserResponse(userRepository.save(user));
    }

    public void deleteUser(String userid) {
        userRepository.deleteById(userid);
    }
}