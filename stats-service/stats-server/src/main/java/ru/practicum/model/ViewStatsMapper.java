package ru.practicum.model;

import lombok.experimental.UtilityClass;
import ru.practicum.dto.ViewStatsDto;

import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class ViewStatsMapper {

    public ViewStatsDto viewStatsToDto(ViewStats viewStats) {
        return ViewStatsDto.builder()
                .uri(viewStats.getUri())
                .app(viewStats.getApp())
                .hits(viewStats.getHits())
                .build();
    }

    public List<ViewStatsDto> listViewStatsToDto(List<ViewStats> viewStats) {
        return viewStats.stream()
                .map(ViewStatsMapper::viewStatsToDto)
                .collect(Collectors.toList());
    }

}
