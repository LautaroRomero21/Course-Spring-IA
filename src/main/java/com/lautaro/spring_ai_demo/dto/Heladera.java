package com.lautaro.spring_ai_demo.dto;

import java.time.Instant;

public record Heladera(
        String id,
        String zona,
        String direccion,
        boolean activa,
        Double tempC,
        Instant lastTempAt,
        int capacidad,
        int stockActual) {
}
