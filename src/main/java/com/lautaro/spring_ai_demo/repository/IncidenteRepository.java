package com.lautaro.spring_ai_demo.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.lautaro.spring_ai_demo.dto.Incidente;
import org.springframework.stereotype.Repository;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Repository
public class IncidenteRepository {

    private final FileStore<Incidente> store = new FileStore<>("data/incidentes.json", new TypeReference<>() {
    });

    public Incidente create(Incidente inc) {
        List<Incidente> all = store.readAll();
        all.add(inc);
        store.writeAll(all);
        return inc;
    }

    public List<Incidente> listAll() {
        return store.readAll();
    }

    public Optional<Incidente> get(String id) {
        return store.readAll().stream()
                .filter(i -> i.id().equalsIgnoreCase(id))
                .findFirst();
    }

    public Incidente getOrThrow(String id) {
        return get(id).orElseThrow(() -> new IllegalArgumentException("No existe incidente con id=" + id));
    }

    public Incidente update(Incidente updated) {
        List<Incidente> all = store.readAll();
        List<Incidente> newList = all.stream()
                .map(i -> i.id().equalsIgnoreCase(updated.id()) ? updated : i)
                .toList();
        store.writeAll(newList);
        return updated;
    }

    public Incidente findLatestOpenOrThrow() {
        return store.readAll().stream()
                .filter(i -> "ABIERTO".equalsIgnoreCase(i.estado()) || "EN_PROCESO".equalsIgnoreCase(i.estado()))
                .max(Comparator.comparing(Incidente::creadoEn))
                .orElseThrow(() -> new IllegalArgumentException("No hay incidentes abiertos"));
    }
}
