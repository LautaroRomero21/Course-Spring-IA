package com.lautaro.spring_ai_demo.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.lautaro.spring_ai_demo.dto.WeatherResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class WeatherApiClient {

    private final RestClient restClient;
    private final String apiKey;

    public WeatherApiClient(
            RestClient.Builder builder,
            @Value("${weatherapi.base-url}") String baseUrl,
            @Value("${weatherapi.api-key}") String apiKey) {
        this.restClient = builder.baseUrl(baseUrl).build();
        this.apiKey = apiKey;
    }

    public WeatherResult currentByCity(String city) {
        WeatherApiResponse apiResponse = this.restClient.get()
                .uri(uri -> uri
                        .path("/current.json")
                        .queryParam("key", apiKey)
                        .queryParam("q", city)
                        .build())
                .retrieve()
                .body(WeatherApiResponse.class);

        if (apiResponse == null || apiResponse.location == null || apiResponse.current == null) {
            throw new IllegalStateException("WeatherAPI devolvió una respuesta vacía o inesperada");
        }

        return new WeatherResult(
                new WeatherResult.Location(apiResponse.location.name, apiResponse.location.country),
                new WeatherResult.Current(apiResponse.current.tempF));
    }

    // DTO interno SOLO para mapear la respuesta real de WeatherAPI (ignoramos el
    // resto de campos)
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class WeatherApiResponse {
        public Location location;
        public Current current;

        @JsonIgnoreProperties(ignoreUnknown = true)
        static class Location {
            public String name;
            public String country;
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        static class Current {
            @JsonProperty("temp_f")
            public double tempF;
        }
    }
}
