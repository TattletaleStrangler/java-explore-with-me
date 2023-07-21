package ru.practicum.client;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class StatsClient {
    private final RestTemplate rest;

    @Value("${stats-server.url}")
    private String serverUrl;

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public StatsClient() {
        rest = new RestTemplate();
        HttpClient httpClient = HttpClients.createDefault();
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setHttpClient(httpClient);
        rest.setRequestFactory(factory);
    }

    public EndpointHitDto createEndpointHit(EndpointHitDto endpointHitDto) {
        EndpointHitDto savedEndpointHit = rest.postForEntity(serverUrl + "/hit", endpointHitDto, EndpointHitDto.class).getBody();
        return savedEndpointHit;
    }

    public List<ViewStatsDto> getStatistics(LocalDateTime start, LocalDateTime end, Boolean unique, List<String> uris) {

        Map<String, Object> parameters = Map.of(
                "start", DTF.format(start),
                "end", DTF.format(end),
                "unique", unique,
                "uris", uris
        );

        String uri = serverUrl + "/stats";
        String url = UriComponentsBuilder.fromHttpUrl(uri)
                .queryParam("start", "{start}")
                .queryParam("end", "{end}")
                .queryParam("unique", "{unique}")
                .queryParam("uris", uris)
                .encode().toUriString();

        try {
            ResponseEntity<ViewStatsDto[]> views = rest.getForEntity(url, ViewStatsDto[].class, parameters);
            List<ViewStatsDto> viewList = List.of(Objects.requireNonNull(views.getBody()));
            return viewList;
        } catch (HttpStatusCodeException e) {
            return List.of();
        }
    }

    private <T> ResponseEntity<Object> makeAndSendRequest(HttpMethod method, String path, @Nullable Map<String, Object> parameters, @Nullable T body) {
        HttpEntity<T> requestEntity = new HttpEntity<>(body, defaultHeaders());

        ResponseEntity<Object> statsServerResponse;
        try {
            if (parameters != null) {
                statsServerResponse = rest.exchange(path, method, requestEntity, Object.class, parameters);
            } else {
                statsServerResponse = rest.exchange(path, method, requestEntity, Object.class);
            }
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsByteArray());
        }
        return prepareResponse(statsServerResponse);
    }

    private HttpHeaders defaultHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }

    private static ResponseEntity<Object> prepareResponse(ResponseEntity<Object> response) {
        if (response.getStatusCode().is2xxSuccessful()) {
            return response;
        }

        ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.status(response.getStatusCode());

        if (response.hasBody()) {
            return responseBuilder.body(response.getBody());
        }

        return responseBuilder.build();
    }

}
