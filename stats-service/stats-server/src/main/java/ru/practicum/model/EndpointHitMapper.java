package ru.practicum.model;

import lombok.experimental.UtilityClass;
import ru.practicum.dto.EndpointHitDto;

@UtilityClass
public class EndpointHitMapper {

    public EndpointHit dtoToEndpointHit(EndpointHitDto dto, App app) {
        return EndpointHit.builder()
                .app(app)
                .ip(dto.getIp())
                .uri(dto.getUri())
                .timestamp(dto.getTimestamp())
                .build();
    }

    public EndpointHitDto endpointHitToDto(EndpointHit endpointHit) {
        return EndpointHitDto.builder()
                .id(endpointHit.getId())
                .app(endpointHit.getApp().getName())
                .ip(endpointHit.getIp())
                .uri(endpointHit.getUri())
                .timestamp(endpointHit.getTimestamp())
                .build();
    }

}
