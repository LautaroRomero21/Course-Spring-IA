package com.lautaro.spring_ai_demo.dto;

public record bookDetails(
        String category,
        String book,
        String year,
        String review,
        String author,
        String summary) {
};