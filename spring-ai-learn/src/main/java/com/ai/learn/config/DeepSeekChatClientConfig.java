package com.ai.learn.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * DeepSeek 模型配置
 * 使用 @Profile("deepseek") 注解，当激活 deepseek profile 时生效
 */
@Configuration
@Profile("deepseek")
public class DeepSeekChatClientConfig {

    /**
     * 配置 DeepSeek ChatClient
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
                .defaultSystem("You are a friendly chat bot that belongs to Qin, powered by DeepSeek")
                .build();
    }
}