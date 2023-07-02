package ru.practicum.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Builder
@Getter
@AllArgsConstructor
@RequiredArgsConstructor
@Table(name = "endpointhit", schema = "public")
public class EndpointHit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "app", length = 255, nullable = false)
    private String app;

    @Column(name = "uri", length = 512, nullable = false)
    private String uri;

    @Column(name = "ip", length = 50, nullable = false)
    private String ip;

    @Column(name = "datetime", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
}
