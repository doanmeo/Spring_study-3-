package com.saleticket.exam1.mapper;

import com.saleticket.exam1.dto.request.EventCreationRequest;
import com.saleticket.exam1.dto.response.EventResponse;
import com.saleticket.exam1.entity.Event;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EventMapper {

    EventResponse toEventResponse(Event event);

    @Mapping(target = "availableTickets", source = "totalTickets") // Khi tạo mới, số vé còn lại bằng tổng số vé
    Event toEvent(EventCreationRequest request);
}
