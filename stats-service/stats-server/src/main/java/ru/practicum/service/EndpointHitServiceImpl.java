package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.model.EndpointHit;
import ru.practicum.model.EndpointHitMapper;
import ru.practicum.model.ViewStats;
import ru.practicum.model.ViewStatsMapper;
import ru.practicum.storage.EndpointHitStorage;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EndpointHitServiceImpl implements EndpointHitService {

    private final EndpointHitStorage endpointHitStorage;

    @Override
    public EndpointHitDto createEndpointHit(EndpointHitDto endpointHitDto) {
        EndpointHit endpointHit = EndpointHitMapper.dtoToEndpointHit(endpointHitDto);
        EndpointHit savedHit = endpointHitStorage.save(endpointHit);
        return EndpointHitMapper.endpointHitToDto(savedHit);
    }

    @Override
    public List<ViewStatsDto> getStatistics(LocalDateTime start, LocalDateTime end, Boolean unique, List<String> uris) {
        checkDates(start, end);

        List<ViewStats> result;

        if (unique) {
            if (uris == null) {
                result = endpointHitStorage.getStatsByUnique(start, end);
            } else {
                result = endpointHitStorage.getStatsByUniqueAndUris(start, end, uris);
            }
        } else {
            if (uris == null) {
                result = endpointHitStorage.getStats(start, end);
            } else {
                result = endpointHitStorage.getStatsByUris(start, end, uris);
            }
        }

        return ViewStatsMapper.listViewStatsToDto(result);
    }

    private void checkDates(LocalDateTime start, LocalDateTime end) {
        if (start.compareTo(end) > 0) {
            throw new IllegalArgumentException("Start date must be earlier than end date");
        }
    }

}
