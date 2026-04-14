package com.ai.learn.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Qwen 模型配置
 * 使用 @Profile("qwen") 注解，当激活 qwen profile 时生效
 */
@Configuration
@Profile("qwen")
public class QwenChatClientConfig {

    /**
     * 配置 Qwen ChatClient
     * <p>
     * 这里使用的是 openai 协议的 ChatModel，因此这里动态注入的 ChatModel 是 OpenAiChatModel
     * 与 spring-ai-chat-deepseek 模块的 DeepSeekChatModel 不同
     * <p>
     * 这里 chatClient 设置了默认的系统提示语，会将所有的聊天请求都带上这个系统提示语
     */
    @Bean
    public ChatClient chatClient(OpenAiChatModel chatModel) {
        // return ChatClient.builder(chatModel).build();
        // build with Default System Text
        return ChatClient.builder(chatModel)
                .defaultSystem("You are a friendly chat bot that belongs to Qin, powered by Qwen")
                .build();
    }
}