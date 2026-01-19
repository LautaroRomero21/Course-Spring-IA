package com.lautaro.spring_ai_demo.service;

import com.lautaro.spring_ai_demo.tools.WeatherTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class WeatherAssistantService {

        private final ChatClient chatClient;
        private final WeatherTools weatherTools;

        public WeatherAssistantService(ChatClient.Builder builder, WeatherTools weatherTools) {
                this.chatClient = builder.build();
                this.weatherTools = weatherTools;
        }

        public String askWeatherText(String city) {
                return chatClient.prompt()
                                .system("""
                                                You are a weather assistant.
                                                You MUST call the tool getCurrentWeather to obtain the real data.
                                                Answer in Spanish, 1-2 sentences, friendly. No JSON. No markdown.
                                                Include city, country and temperature in °F.
                                                """)
                                .user(u -> u.text("What's the current weather in {city}?")
                                                .param("city", city))
                                .tools(weatherTools)
                                .call()
                                .content();
        }

}
