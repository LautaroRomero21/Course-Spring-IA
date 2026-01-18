package com.lautaro.spring_ai_demo.controller;

import com.lautaro.spring_ai_demo.service.VisionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/vision")
public class VisionController {

    private final VisionService visionService;

    public VisionController(VisionService visionService) {
        this.visionService = visionService;
    }

    @GetMapping("/ask")
    public String ask(
            @RequestParam String image,
            @RequestParam String question) {
        return visionService.askAboutImage(image, question);
    }
}
