package com.ai.learn.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 默认 ChatClient 配置
 * 当没有激活特定 profile 时使用此配置
 * 默认使用 application.properties 中的配置（通常是 DeepSeek）
 */
@Configuration
public class DefaultChatClientConfig {

    /**
     * 默认 ChatClient 配置
     * <p>
     * 使用 @ConditionalOnMissingBean 确保只有当其他 ChatClient bean 不存在时才创建此 bean
     * 这样当激活 qwen 或 deepseek profile 时，此配置不会生效
     * 使用 application.properties 中的配置，默认是 DeepSeek
     * 可以通过设置 spring.profiles.active 来切换不同的配置
     */
    @Bean
    @ConditionalOnMissingBean(ChatClient.class)
    public ChatClient chatClient(OpenAiChatModel chatModel) {
        return ChatClient.builder(chatModel)
                .defaultSystem("You are a friendly chat bot that belongs to Qin")
                .build();
    }
}