package com.lautaro.spring_ai_demo.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.util.*;

@Configuration
public class AiConfig {

    @Bean(name = "kbVectorStore")
    public VectorStore kbVectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }

    @Bean
    public CommandLineRunner ingestKbDocs(@Qualifier("kbVectorStore") VectorStore kbVectorStore) {
        return args -> {
            Resource[] resources = new PathMatchingResourcePatternResolver()
                    .getResources("classpath:/kb/*.md");

            TokenTextSplitter splitter = new TokenTextSplitter();
            List<Document> chunks = new ArrayList<>();

            for (Resource r : resources) {
                TextReader reader = new TextReader(r);
                reader.getCustomMetadata().put("type", "kb");
                reader.getCustomMetadata().put("source", r.getFilename());
                chunks.addAll(splitter.apply(reader.read()));
            }

            kbVectorStore.add(chunks);
        };
    }

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder,
            @Qualifier("kbVectorStore") VectorStore kbVectorStore) {

        var safe = SafeGuardAdvisor.builder()
                .sensitiveWords(List.of("api-key", "password", "secret", "token"))
                .failureResponse("No puedo ayudar con informacion sensible.")
                .order(Ordered.HIGHEST_PRECEDENCE)
                .build();

        var rag = QuestionAnswerAdvisor.builder(kbVectorStore)
                .searchRequest(SearchRequest.builder()
                        .topK(4)
                        .similarityThreshold(0.70)
                        .build())
                .build();

        return builder
                .defaultAdvisors(safe, rag)
                .build();
    }
}
