package com.saleticket.exam1.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.saleticket.exam1.dto.request.UserCreationRequest;
import com.saleticket.exam1.dto.request.UserUpdateRequest;
import com.saleticket.exam1.dto.response.UserResponse;
import com.saleticket.exam1.entity.User;
@Mapper(componentModel = "spring")
public interface UserMapper {
       @Mapping(target = "roles", expression = "java(user.getRoles().stream().map(Role::getName).collect(java.util.stream.Collectors.toSet()))")
       UserResponse toUserResponse(User user);

       User toUser(UserCreationRequest request);

       @Mapping(target = "roles", ignore = true)
       void updateUser(UserUpdateRequest request, @MappingTarget User user);
}
