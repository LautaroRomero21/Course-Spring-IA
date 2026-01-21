package com.lautaro.spring_ai_demo.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class AssistantController {

    private final ChatClient ragChatClient;

    public AssistantController(ChatClient ragChatClient) {
        this.ragChatClient = ragChatClient;
    }

    @GetMapping("/chat")
    public String chat(
            @RequestParam String q,
            @RequestParam(defaultValue = "default") String cid) {
        return ragChatClient.prompt()
                .system("""
                        Sos un asistente útil.
                        Si te falta contexto, decilo.
                        Cuando uses contexto recuperado, integralo naturalmente en la respuesta.
                        """)
                .advisors(a -> a
                        .param(ChatMemory.CONVERSATION_ID, cid)
                        .param(QuestionAnswerAdvisor.FILTER_EXPRESSION, "type == 'kb'"))
                .user(q)
                .call()
                .content();
    }
}
