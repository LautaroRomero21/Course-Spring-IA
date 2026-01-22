package com.lautaro.spring_ai_demo.tools;

import com.lautaro.spring_ai_demo.dto.Heladera;
import com.lautaro.spring_ai_demo.dto.Incidente;
import com.lautaro.spring_ai_demo.dto.Tecnico;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.*;

@Component
public class HeladeraTools {

    // Para simplificar aca, asumi que tenes repos inyectados:
    // HeladeraRepository, IncidenteRepository, TecnicoRepository
    private final com.lautaro.spring_ai_demo.repository.HeladeraRepository heladeras;
    private final com.lautaro.spring_ai_demo.repository.IncidenteRepository incidentes;
    private final com.lautaro.spring_ai_demo.repository.TecnicoRepository tecnicos;

    public HeladeraTools(
            com.lautaro.spring_ai_demo.repository.HeladeraRepository heladeras,
            com.lautaro.spring_ai_demo.repository.IncidenteRepository incidentes,
            com.lautaro.spring_ai_demo.repository.TecnicoRepository tecnicos) {
        this.heladeras = heladeras;
        this.incidentes = incidentes;
        this.tecnicos = tecnicos;
    }

    @Tool(description = "List community fridges by zone.")
    public List<Heladera> listarHeladerasPorZona(
            @ToolParam(description = "Zone name, e.g. Palermo") String zona) {
        return heladeras.listByZona(zona);
    }

    @Tool(description = "Report an incident for a given fridge.")
    public Incidente reportarIncidente(
            @ToolParam(description = "Fridge id, e.g. H001") String heladeraId,
            @ToolParam(description = "Incident type, e.g. TEMPERATURA_ALTA, INACTIVA, VACIA") String tipo,
            @ToolParam(description = "Short description") String descripcion) {

        Incidente i = new Incidente(
                UUID.randomUUID().toString(),
                heladeraId,
                tipo,
                descripcion,
                "ABIERTO",
                null,
                Instant.now(),
                Instant.now());
        return incidentes.create(i);
    }

    @Tool(description = "Assign a technician to an incident (same zone if possible).")
    public Incidente asignarTecnico(
            @ToolParam(description = "Incident id") String incidenteId) {

        Incidente inc = incidentes.getOrThrow(incidenteId);
        Heladera h = heladeras.getOrThrow(inc.heladeraId());

        Optional<Tecnico> t = tecnicos.findByZona(h.zona());
        if (t.isEmpty())
            return inc;

        Incidente updated = new Incidente(
                inc.id(),
                inc.heladeraId(),
                inc.tipo(),
                inc.descripcion(),
                "EN_PROCESO",
                t.get().id(),
                inc.creadoEn(),
                Instant.now());
        return incidentes.update(updated);
    }

    @Tool(description = "Assign a technician to the most recent OPEN incident.")
    public Incidente asignarTecnicoUltimoIncidenteAbierto() {

        Incidente inc = incidentes.findLatestOpenOrThrow();
        Heladera h = heladeras.getOrThrow(inc.heladeraId());

        Optional<Tecnico> t = tecnicos.findByZona(h.zona());
        if (t.isEmpty())
            return inc;

        Incidente updated = new Incidente(
                inc.id(),
                inc.heladeraId(),
                inc.tipo(),
                inc.descripcion(),
                "EN_PROCESO",
                t.get().id(),
                inc.creadoEn(),
                Instant.now());

        return incidentes.update(updated);
    }

}
