package com.lautaro.spring_ai_demo.controller;

import com.lautaro.spring_ai_demo.service.BikeSearchService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bikes")
public class BikeController {

    private final BikeSearchService bikeSearchService;

    public BikeController(BikeSearchService bikeSearchService) {
        this.bikeSearchService = bikeSearchService;
    }

    @GetMapping("/search")
    public List<BikeSearchService.BikeMatch> search(
            @RequestParam String query,
            @RequestParam(defaultValue = "3") int topK) {
        return bikeSearchService.search(query, topK);
    }
}
