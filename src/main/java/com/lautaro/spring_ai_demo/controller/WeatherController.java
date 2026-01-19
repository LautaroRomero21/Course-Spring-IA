package com.lautaro.spring_ai_demo.controller;

import com.lautaro.spring_ai_demo.service.WeatherAssistantService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WeatherController {

    private final WeatherAssistantService weatherAssistantService;

    public WeatherController(WeatherAssistantService weatherAssistantService) {
        this.weatherAssistantService = weatherAssistantService;
    }

    @GetMapping("/api/v1/weather")
    public String weather(@RequestParam String city) {
        return weatherAssistantService.askWeatherText(city);
    }

}
