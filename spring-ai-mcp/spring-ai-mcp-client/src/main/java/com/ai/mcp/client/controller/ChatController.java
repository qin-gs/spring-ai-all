package com.ai.mcp.client.controller;

import com.ai.mcp.client.advisor.PlanExecutionAdvisor;
import jakarta.servlet.http.HttpSession;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("mcp")
public class ChatController {

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private ChatMemory chatMemory;

    /**
     * 聊天接口，支持多轮对话
     * <p>
     * 会话由 HttpSession 自动管理，客户端无需传参
     */
    @GetMapping("chat")
    public String prompt(
            @RequestParam String userInput,
            HttpSession session) {

        return this.chatClient.prompt()
                .advisors(
                        MessageChatMemoryAdvisor.builder(chatMemory)
                                .conversationId(session.getId())
                                .build(),
                        new PlanExecutionAdvisor(300)
                )
                .user(userInput)
                .call()
                .content();
    }
}
