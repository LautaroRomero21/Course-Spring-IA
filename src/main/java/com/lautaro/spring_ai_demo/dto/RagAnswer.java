package com.lautaro.spring_ai_demo.dto;

import java.util.List;

public record RagAnswer(String answer, List<String> sources) {
}
