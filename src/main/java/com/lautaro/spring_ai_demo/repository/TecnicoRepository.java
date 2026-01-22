package com.lautaro.spring_ai_demo.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.lautaro.spring_ai_demo.dto.Tecnico;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TecnicoRepository {

    private final FileStore<Tecnico> store = new FileStore<>("data/tecnicos.json", new TypeReference<>() {
    });

    public List<Tecnico> listAll() {
        return store.readAll();
    }

    public Optional<Tecnico> findByZona(String zona) {
        String z = zona == null ? "" : zona.trim().toLowerCase();
        return store.readAll().stream()
                .filter(t -> t.zona() != null && t.zona().trim().toLowerCase().equals(z))
                .findFirst();
    }
}
