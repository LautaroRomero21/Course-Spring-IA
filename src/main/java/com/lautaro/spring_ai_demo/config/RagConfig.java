package com.lautaro.spring_ai_demo.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class RagConfig {

    // Ingesta: lee todos los .md de classpath:/rag, los parte en chunks y los
    // guarda
    // No interfiere con el vectorStore de los demas archivos, esto solo sirve como
    // base de las consultas RAG.
    // aunque si tenemos varias consultas RAG entonces lo recomendado para que no se
    // pisen es primero poner metaadata en los archivos a leer.
    // con esa metadata depsues puedo hacer
    // reader.getCustomMetadata().put("collection", "heladeras"); y filtras de
    // entrada
    @Bean
    public org.springframework.boot.CommandLineRunner ingestRagDocs(VectorStore vectorStore) {
        return args -> {
            Resource[] resources = new PathMatchingResourcePatternResolver()
                    .getResources("classpath:/rag/*.md");

            List<Document> allChunks = new ArrayList<>();
            TokenTextSplitter splitter = new TokenTextSplitter();

            for (Resource r : resources) {
                TextReader reader = new TextReader(r);
                reader.getCustomMetadata().put("source", r.getFilename());

                List<Document> docs = reader.read();
                List<Document> chunks = splitter.apply(docs);

                // aseguramos metadata "source" en cada chunk
                for (Document d : chunks) {
                    d.getMetadata().putIfAbsent("source", r.getFilename());
                }

                allChunks.addAll(chunks);
            }

            vectorStore.add(allChunks);
        };
    }

    // Advisor: Improved RAG (query rewrite + retrieval + (opcional) permitir
    // contexto vacío)
    @Bean
    public RetrievalAugmentationAdvisor ragAdvisor(ChatClient.Builder chatClientBuilder,
            VectorStore vectorStore) {

        // 1) Query rewrite (mejora la consulta antes de buscar)
        var rewrite = RewriteQueryTransformer.builder()
                .chatClientBuilder(chatClientBuilder.build().mutate())
                .build();

        // 2) Retriever (topK + threshold + filtros si quisiera)
        // resumen: obtiene “los pedazos correctos” de los docs.
        var retriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .topK(6)
                .similarityThreshold(0.60)
                .build();

        // 3) Cómo se “pega” el contexto al prompt (si no hay contexto, por defecto NO
        // responde)
        var augmenter = ContextualQueryAugmenter.builder()
                .allowEmptyContext(true) // en POC es cómodo; en prod quizá false
                .build();

        return RetrievalAugmentationAdvisor.builder()
                .queryTransformers(rewrite)
                .documentRetriever(retriever)
                .queryAugmenter(augmenter)
                .build();
    }
}
