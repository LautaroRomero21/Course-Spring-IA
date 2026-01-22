package com.lautaro.spring_ai_demo.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class FileStore<T> {

    private final ObjectMapper mapper;
    private final File file;
    private final TypeReference<List<T>> typeRef;

    public FileStore(String path, TypeReference<List<T>> typeRef) {
        this.file = new File(path);
        this.typeRef = typeRef;
        this.mapper = new ObjectMapper()
                .findAndRegisterModules() // ✅ JavaTimeModule (Instant, LocalDateTime, etc.)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // ✅ ISO-8601
        ensureDir();
    }

    public synchronized List<T> readAll() {
        try {
            if (!file.exists())
                return new ArrayList<>();
            return mapper.readValue(file, typeRef);
        } catch (Exception e) {
            // Si el JSON quedo roto por un crash anterior, mejor arrancar vacio
            return new ArrayList<>();
        }
    }

    public synchronized void writeAll(List<T> data) {
        try {
            // Escritura atomica: evita archivos truncados si algo falla a mitad
            File tmp = new File(file.getAbsolutePath() + ".tmp");
            mapper.writerWithDefaultPrettyPrinter().writeValue(tmp, data);
            Files.move(tmp.toPath(), file.toPath(),
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo escribir " + file.getAbsolutePath(), e);
        }
    }

    private void ensureDir() {
        File dir = file.getParentFile();
        if (dir != null)
            dir.mkdirs();
    }
}
