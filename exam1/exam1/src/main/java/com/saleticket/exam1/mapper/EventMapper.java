package com.saleticket.exam1.mapper;

import com.saleticket.exam1.dto.request.EventCreationRequest;
import com.saleticket.exam1.dto.request.EventUpdateRequest;
import com.saleticket.exam1.dto.request.UserUpdateRequest;
import com.saleticket.exam1.dto.response.EventResponse;
import com.saleticket.exam1.entity.Event;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface EventMapper {

    EventResponse toEventResponse(Event event);

    @Mapping(target = "availableTickets", source = "totalTickets")
    Event toEvent(EventCreationRequest request);

    void updateEvent(EventUpdateRequest request, @MappingTarget Event event);
}
