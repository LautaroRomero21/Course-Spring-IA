package com.lautaro.spring_ai_demo.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.lautaro.spring_ai_demo.dto.Heladera;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class HeladeraRepository {

    private final FileStore<Heladera> store = new FileStore<>("data/heladeras.json", new TypeReference<>() {
    });

    public List<Heladera> listAll() {
        return store.readAll();
    }

    public List<Heladera> listByZona(String zona) {
        String z = zona == null ? "" : zona.trim().toLowerCase();
        return store.readAll().stream()
                .filter(h -> h.zona() != null && h.zona().trim().toLowerCase().equals(z))
                .toList();
    }

    public Optional<Heladera> get(String id) {
        return store.readAll().stream()
                .filter(h -> h.id().equalsIgnoreCase(id))
                .findFirst();
    }

    public Heladera getOrThrow(String id) {
        return get(id).orElseThrow(() -> new IllegalArgumentException("No existe heladera con id=" + id));
    }

    public void saveAll(List<Heladera> heladeras) {
        store.writeAll(heladeras);
    }
}
