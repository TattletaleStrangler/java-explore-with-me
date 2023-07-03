package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.model.*;
import ru.practicum.storage.AppStorage;
import ru.practicum.storage.EndpointHitStorage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EndpointHitServiceImpl implements EndpointHitService {

    private final EndpointHitStorage endpointHitStorage;
    private final AppStorage appStorage;

    @Override
    public EndpointHitDto createEndpointHit(EndpointHitDto endpointHitDto) {
        App app = getOrCreate(endpointHitDto.getApp());

        EndpointHit endpointHit = EndpointHitMapper.dtoToEndpointHit(endpointHitDto, app);
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

    private App getOrCreate(String appName) {
        Optional<App> app = appStorage.findByName(appName);

        if (app.isPresent()) {
            return app.get();
        } else {
            App newApp = new App();
            newApp.setName(appName);
            return appStorage.save(newApp);
        }
    }

}
