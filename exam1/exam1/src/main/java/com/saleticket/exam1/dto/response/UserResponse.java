package com.saleticket.exam1.dto.response;

import java.util.Set;

import com.saleticket.exam1.entity.Role;

public record UserResponse(
    String id,
    String username,
    String email,
    Set<Role> roles // Ta chỉ cần trả về danh sách tên quyền (VD: ["ROLE_USER"])
) {}