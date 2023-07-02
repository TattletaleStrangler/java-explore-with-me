package ru.practicum.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
public class ViewStatsDto {
    private String app;
    private String uri;
    private Long hits;

}
