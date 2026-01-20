package com.lautaro.spring_ai_demo.service;

import com.lautaro.spring_ai_demo.dto.RagAnswer;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RagAssistantService {

        private final ChatClient chatClient;
        private final RetrievalAugmentationAdvisor ragAdvisor;

        public RagAssistantService(ChatClient.Builder builder,
                        RetrievalAugmentationAdvisor ragAdvisor) {
                this.chatClient = builder.build();
                this.ragAdvisor = ragAdvisor;
        }

        public RagAnswer ask(String question) {

                ChatClientResponse resp = chatClient.prompt()
                                .system("""
                                                Sos un asistente. Respondé usando SOLO el contexto recuperado.
                                                Si el contexto no alcanza, decí: "No tengo esa info en mis documentos".
                                                """)
                                .advisors(ragAdvisor)
                                .user(question)
                                .call()
                                .chatClientResponse(); // para leer context + docs usados

                String answer = resp.chatResponse().getResult().getOutput().getText();

                // este bloque esta de mas, solo devuelve que archivo verifico
                @SuppressWarnings("unchecked")
                List<Document> docsUsed = (List<Document>) resp.context()
                                .get(RetrievalAugmentationAdvisor.DOCUMENT_CONTEXT);

                var sources = (docsUsed == null ? List.<String>of()
                                : docsUsed.stream()
                                                .map(d -> String.valueOf(
                                                                d.getMetadata().getOrDefault("source", "unknown")))
                                                .distinct()
                                                .toList());
                // esto esta de mas, solo devuelve que archivo verifico

                return new RagAnswer(answer, sources);
        }
}
