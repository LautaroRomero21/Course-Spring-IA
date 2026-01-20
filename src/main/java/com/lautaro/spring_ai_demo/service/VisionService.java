package com.lautaro.spring_ai_demo.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;

@Service
public class VisionService {

    private final ChatClient chatClient;

    public VisionService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    public String askAboutImage(String imageName, String question) {
        Resource img = new ClassPathResource("images/" + imageName);

        return chatClient.prompt()
                .system("You are a helpful assistant. Answer in Spanish.")
                .user(u -> u
                        .text("{question}")
                        .param("question", question)
                        .media(mimeTypeFor(imageName), img))
                .call()
                .content();
    }

    private static MimeType mimeTypeFor(String fileName) {
        String f = fileName.toLowerCase();
        if (f.endsWith(".png"))
            return MimeTypeUtils.IMAGE_PNG;
        if (f.endsWith(".jpg") || f.endsWith(".jpeg"))
            return MimeTypeUtils.IMAGE_JPEG;
        if (f.endsWith(".gif"))
            return MimeTypeUtils.IMAGE_GIF;
        if (f.endsWith(".webp"))
            return new MimeType("image", "webp");
        throw new IllegalArgumentException("Formato de imagen no soportado: " + fileName);
    }
}
