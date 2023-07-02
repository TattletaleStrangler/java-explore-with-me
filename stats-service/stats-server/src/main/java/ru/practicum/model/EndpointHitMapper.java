package ru.practicum.model;

import lombok.experimental.UtilityClass;
import ru.practicum.dto.EndpointHitDto;

@UtilityClass
public class EndpointHitMapper {

    public EndpointHit dtoToEndpointHit(EndpointHitDto dto) {
        return EndpointHit.builder()
                .app(dto.getApp())
                .ip(dto.getIp())
                .uri(dto.getUri())
                .timestamp(dto.getTimestamp())
                .build();
    }

    public EndpointHitDto endpointHitToDto(EndpointHit endpointHit) {
        EndpointHitDto endpointHitDto = new EndpointHitDto();
        endpointHitDto.setId(endpointHit.getId());
        endpointHitDto.setApp(endpointHit.getApp());
        endpointHitDto.setIp(endpointHit.getIp());
        endpointHitDto.setUri(endpointHit.getUri());
        endpointHitDto.setTimestamp(endpointHit.getTimestamp());
        return endpointHitDto;
    }

}
