package com.lautaro.spring_ai_demo.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.*;

@Repository
public class ConversationRepository {

    public record Turn(String role, String content, Instant at) {
    }

    private final FileStore<Map<String, List<Turn>>> store = new FileStore<>("data/conversations.json",
            new TypeReference<>() {
            });

    public synchronized List<Turn> get(String cid) {
        Map<String, List<Turn>> all = getAll();
        return all.getOrDefault(cid, new ArrayList<>());
    }

    public synchronized void append(String cid, String role, String content) {
        Map<String, List<Turn>> all = getAll();
        List<Turn> turns = all.getOrDefault(cid, new ArrayList<>());
        turns.add(new Turn(role, content, Instant.now()));

        // Limitar a ultimos 12 turnos (para no crecer infinito)
        if (turns.size() > 12)
            turns = turns.subList(turns.size() - 12, turns.size());

        all.put(cid, new ArrayList<>(turns));
        store.writeAll(List.of(all));
    }

    private Map<String, List<Turn>> getAll() {
        List<Map<String, List<Turn>>> list = store.readAll();
        if (list.isEmpty())
            return new HashMap<>();
        return list.get(0);
    }
}
