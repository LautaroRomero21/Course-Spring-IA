package com.lautaro.spring_ai_demo.dto;

import java.time.Instant;

public record Incidente(
        String id,
        String heladeraId,
        String tipo, // TEMPERATURA_ALTA, INACTIVA, PUERTA_FORZADA, VACIA, etc
        String descripcion,
        String estado, // ABIERTO, EN_PROCESO, RESUELTO
        String tecnicoId,
        Instant creadoEn,
        Instant actualizadoEn) {
}
