package com.lautaro.spring_ai_demo.controller;

import com.lautaro.spring_ai_demo.service.AssistantService;
import org.springframework.web.bind.annotation.*;
import com.lautaro.spring_ai_demo.dto.ChatRequest;
import com.lautaro.spring_ai_demo.dto.ChatResponse;

@RestController
@RequestMapping("/api/v1")
public class AssistantController {

    private final AssistantService assistant;

    public AssistantController(AssistantService assistant) {
        this.assistant = assistant;
    }

    @PostMapping("/chat")
    public ChatResponse chat(@RequestBody ChatRequest req) {
        String cid = (req.cid() == null || req.cid().isBlank()) ? "default" : req.cid();
        return new ChatResponse(cid, assistant.chat(cid, req.q()));
    }

}
