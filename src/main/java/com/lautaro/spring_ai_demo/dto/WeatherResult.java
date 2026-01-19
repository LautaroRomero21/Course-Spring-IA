package com.lautaro.spring_ai_demo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

public record WeatherResult(Location location, Current current) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Location(String name, String country) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Current(@JsonProperty("temp_f") double tempF) {
    }
}
