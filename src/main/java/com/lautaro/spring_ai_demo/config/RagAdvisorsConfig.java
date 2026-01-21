package com.lautaro.spring_ai_demo.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.beans.factory.annotation.Qualifier;

@Configuration
public class RagAdvisorsConfig {

    @Bean(name = "kbVectorStore")
    public VectorStore kbVectorStore(EmbeddingModel embeddingModel) {
        // VectorStore en memoria (simple) para demo rápida
        return SimpleVectorStore.builder(embeddingModel).build();
    }

    @Bean
    public CommandLineRunner ingestKbDocs(@Qualifier("kbVectorStore") VectorStore kbVectorStore) {
        return args -> {
            // Lee todos los markdown de /resources/rag/*.md
            List<Document> docs = new MarkdownDocumentReader("classpath:/rag/*.md").get();

            // IMPORTANTE: marcamos estos docs como "kb" para poder filtrarlos en el advisor
            List<Document> kbDocs = docs.stream().map(d -> {
                Map<String, Object> md = new HashMap<>(d.getMetadata());
                md.put("type", "kb");
                return new Document(d.getText(), md);
            }).toList();

            kbVectorStore.add(kbDocs);
        };
    }

    @Bean
    public ChatClient ragChatClient(
            ChatClient.Builder builder,
            ChatMemory chatMemory,
            @Qualifier("kbVectorStore") VectorStore kbVectorStore) {

        var safeGuard = SafeGuardAdvisor.builder()
                .sensitiveWords(List.of("api-key", "password", "secret"))
                .failureResponse("No puedo ayudar con información sensible.")
                // para que corra antes que memoria y RAG
                .order(Ordered.HIGHEST_PRECEDENCE)
                .build();

        var memory = MessageChatMemoryAdvisor.builder(chatMemory).build();

        var rag = QuestionAnswerAdvisor.builder(kbVectorStore)
                .searchRequest(SearchRequest.builder()
                        .topK(4)
                        .similarityThreshold(0.75)
                        .build())
                .build();

        return builder
                .defaultAdvisors(safeGuard, memory, rag)
                .build();
    }
}
