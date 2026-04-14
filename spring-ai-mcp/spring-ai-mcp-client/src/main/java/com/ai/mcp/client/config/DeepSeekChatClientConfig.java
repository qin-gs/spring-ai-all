package com.ai.mcp.client.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DeepSeekChatClientConfig {

    @Bean
    public ChatClient chatClient(OpenAiChatModel chatModel) {
        // return ChatClient.builder(chatModel).build();
        // build with Default System Text
        return ChatClient.builder(chatModel)
            .defaultSystem("You are a friendly chat bot that belongs to Qin")
            .build();
    }
}