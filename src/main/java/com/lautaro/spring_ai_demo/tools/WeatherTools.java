package com.lautaro.spring_ai_demo.tools;

import com.lautaro.spring_ai_demo.dto.WeatherResult;
import com.lautaro.spring_ai_demo.service.WeatherApiClient;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class WeatherTools {

    private final WeatherApiClient weatherApiClient;

    public WeatherTools(WeatherApiClient weatherApiClient) {
        this.weatherApiClient = weatherApiClient;
    }

    @Tool(description = "Get current weather by city using WeatherAPI. Returns location{name,country} and current{temp_f}.")
    public WeatherResult getCurrentWeather(
            @ToolParam(description = "City name, e.g. 'Buenos Aires'") String city) {
        return weatherApiClient.currentByCity(city);
    }
}
