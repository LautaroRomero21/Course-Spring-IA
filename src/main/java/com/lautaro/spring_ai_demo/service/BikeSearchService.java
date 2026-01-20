package com.lautaro.spring_ai_demo.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lautaro.spring_ai_demo.dto.BikeDTO;
import jakarta.annotation.PostConstruct;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BikeSearchService {

    private final VectorStore vectorStore;
    private final ObjectMapper objectMapper;

    // Para devolver el Bike completo a partir del id
    private final Map<String, BikeDTO> bikesById = new HashMap<>();

    public BikeSearchService(VectorStore vectorStore, ObjectMapper objectMapper) {
        this.vectorStore = vectorStore;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void indexBikes() throws Exception {
        try (InputStream is = new ClassPathResource("bikes.json").getInputStream()) {
            List<BikeDTO> bikes = objectMapper.readValue(is, new TypeReference<>() {
            });
            List<Document> docs = new ArrayList<>();

            for (BikeDTO b : bikes) {
                String id = slug(b.name());
                bikesById.put(id, b);

                String textToEmbed = buildText(b);
                Map<String, Object> metadata = Map.of(
                        "name", b.name(),
                        "price", b.price() != null ? b.price().toString() : null,
                        "tags", b.tags() != null ? String.join(",", b.tags()) : "");

                docs.add(new Document(id, textToEmbed, metadata));
            }

            vectorStore.add(docs);
        }
    }

    public List<BikeMatch> search(String query, int topK) {
        var req = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .build();

        List<Document> results = vectorStore.similaritySearch(req);

        return results.stream()
                .map(d -> {
                    BikeDTO bike = bikesById.get(d.getId());
                    double score = d.getScore() != null ? d.getScore() : 0.0;
                    return new BikeMatch(bike, score);
                })
                .collect(Collectors.toList());
    }

    private String buildText(BikeDTO b) {
        // TIP: no meter TODO el megatexto si es enorme; con name + shortDescription +
        // tags suele alcanzar.
        String desc = b.description() == null ? "" : b.description();
        desc = desc.length() > 800 ? desc.substring(0, 800) : desc;

        return """
                NAME: %s
                SHORT: %s
                TAGS: %s
                DETAILS: %s
                """.formatted(
                b.name(),
                safe(b.shortDescription()),
                b.tags() == null ? "" : String.join(", ", b.tags()),
                desc);
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private String slug(String s) {
        return s.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
    }

    public record BikeMatch(BikeDTO bike, double score) {
    }
}
