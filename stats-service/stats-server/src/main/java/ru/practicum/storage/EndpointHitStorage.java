package ru.practicum.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.model.EndpointHit;
import ru.practicum.model.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

public interface EndpointHitStorage extends JpaRepository<EndpointHit, Long> {

    @Query("SELECT new ru.practicum.model.ViewStats(hit.app, hit.uri, COUNT(hit.ip)) FROM EndpointHit AS hit " +
            "WHERE hit.timestamp BETWEEN :start AND :end " +
            "GROUP BY hit.uri ORDER BY COUNT(hit.ip) DESC")
    List<ViewStats> getStats(@Param("start") LocalDateTime start,
                             @Param("end") LocalDateTime end);

    @Query("SELECT new ru.practicum.model.ViewStats(hit.app, hit.uri, COUNT(distinct hit.ip)) FROM EndpointHit AS hit " +
            "WHERE hit.timestamp BETWEEN :start AND :end " +
            "GROUP BY hit.uri ORDER BY COUNT(distinct hit.ip) DESC")
    List<ViewStats> getStatsByUnique(@Param("start") LocalDateTime start,
                             @Param("end") LocalDateTime end);

    @Query("SELECT new ru.practicum.model.ViewStats(hit.app, hit.uri, COUNT(hit.ip)) FROM EndpointHit AS hit " +
            "WHERE hit.timestamp BETWEEN :start AND :end AND hit.uri in :uris " +
            "GROUP BY hit.uri ORDER BY COUNT(hit.ip)  DESC")
    List<ViewStats> getStatsByUris(@Param("start") LocalDateTime start,
                                      @Param("end") LocalDateTime end,
                                    @Param("uris") List<String> uris);

    @Query("SELECT new ru.practicum.model.ViewStats(hit.app, hit.uri, COUNT(distinct hit.ip)) FROM EndpointHit AS hit " +
            "WHERE hit.timestamp BETWEEN :start AND :end AND hit.uri in :uris " +
            "GROUP BY hit.uri ORDER BY COUNT(distinct hit.ip) DESC")
    List<ViewStats> getStatsByUniqueAndUris(@Param("start") LocalDateTime start,
                                    @Param("end") LocalDateTime end,
                                    @Param("uris") List<String> uris);

}
