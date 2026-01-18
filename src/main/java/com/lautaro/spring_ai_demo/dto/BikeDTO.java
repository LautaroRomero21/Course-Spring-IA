package com.lautaro.spring_ai_demo.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.math.BigDecimal;
import java.util.List;

public record BikeDTO(
        String name,
        String shortDescription,
        @JsonAlias({
                "description", "text" }) String description,
        BigDecimal price,
        List<String> tags) {
}
