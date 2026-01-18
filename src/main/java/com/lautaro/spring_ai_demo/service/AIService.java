package com.lautaro.spring_ai_demo.service;

import com.lautaro.spring_ai_demo.dto.bookDetails;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class AIService {

        private final ChatClient chatClient;

        public AIService(ChatClient.Builder builder) {
                this.chatClient = builder.build();
        }

        public String getJoke(String topic) {
                return chatClient.prompt()
                                .user(u -> u.text("""
                                                Please act as a funny person and create a joke on the given {topic}.
                                                Please be mindful and sensitive about the content though.
                                                """).param("topic", topic))
                                .call()
                                .content();
        }

        public String getBooks(String category, String year) {
                return chatClient.prompt()
                                .user(u -> u.text(
                                                """
                                                                Please provide me best book for the given {category} and the {year}.
                                                                Please do provide a summary of the book as well, the information should be
                                                                limited and not much in depth. Please provide the details in JSON format
                                                                containing: category, book, year, review, author, summary
                                                                """)
                                                .param("category", category)
                                                .param("year", year))
                                .call()
                                .content();
        }

        public bookDetails getBooksInJson(String category, String year) {
                return chatClient.prompt()
                                .system("""
                                                Return ONLY valid JSON. No markdown, no extra text.
                                                Keys: category, book, year, review, author, summary.
                                                """)
                                .user(u -> u.text("""
                                                Provide the best book for category "{category}" in year "{year}".
                                                """)
                                                .param("category", category)
                                                .param("year", year))
                                .call()
                                .entity(bookDetails.class);
        }

}
