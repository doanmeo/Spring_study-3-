package com.saleticket.exam1.mapper;


import org.mapstruct.Mapper;

import com.saleticket.exam1.dto.request.UserCreationRequest;
import com.saleticket.exam1.dto.response.UserResponse;
import com.saleticket.exam1.entity.User;
@Mapper(componentModel = "spring")
public interface UserMapper {

       UserResponse toUserResponse(User user);
}
