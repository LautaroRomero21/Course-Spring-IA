package com.lautaro.spring_ai_demo.controller;

import com.lautaro.spring_ai_demo.dto.RagAnswer;
import com.lautaro.spring_ai_demo.service.RagAssistantService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RagController {

    private final RagAssistantService rag;

    public RagController(RagAssistantService rag) {
        this.rag = rag;
    }

    @GetMapping("/api/v1/rag/ask")
    public RagAnswer ask(@RequestParam String q) {
        return rag.ask(q);
    }
}
