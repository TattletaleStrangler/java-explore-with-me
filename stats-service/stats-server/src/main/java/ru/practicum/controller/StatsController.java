package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.service.EndpointHitService;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping
public class StatsController {

    private final EndpointHitService endpointHitService;

    @PostMapping("/hit")
    EndpointHitDto createEndpointHit(@Valid @RequestBody EndpointHitDto endpointHitDto) {
        log.info("Post /hit");
        return endpointHitService.createEndpointHit(endpointHitDto);
    }

    @GetMapping("/stats")
    List<ViewStatsDto> getStatistics(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
                                     @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
                                     @RequestParam(required = false, defaultValue = "false") Boolean unique,
                                     @RequestParam(required = false) List<String> uris) {
        log.info("Get /stats with params start={}, end={}, unique={}, uris={}", start, end, unique, uris);
        return endpointHitService.getStatistics(start, end, unique, uris);
    }

}
