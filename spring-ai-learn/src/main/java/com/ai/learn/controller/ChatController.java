package com.ai.learn.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("qwen")
public class ChatController {

    @Autowired
    private ChatClient chatClient;

    /**
     * 普通聊天接口
     *
     * @param userInput 用户输入
     * @return 返回内容
     */
    @GetMapping("chat")
    public String prompt(@RequestParam String userInput) {
        return this.chatClient.prompt().user(userInput).call().content();
    }
}
