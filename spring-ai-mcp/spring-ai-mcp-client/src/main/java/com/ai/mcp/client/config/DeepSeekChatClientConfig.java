package com.ai.mcp.client.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.stream.Collectors;

@Configuration
public class DeepSeekChatClientConfig {

    @Bean
    public ChatClient chatClient(OpenAiChatModel chatModel, ToolCallbackProvider toolCallbackProvider) {
        ToolCallback[] callbacks = toolCallbackProvider.getToolCallbacks();
        String toolList = Arrays.stream(callbacks)
                .map(tc -> "- " + tc.getToolDefinition().name() + "：" + tc.getToolDefinition().description())
                .collect(Collectors.joining("\n"));

        String systemPrompt = """
                你是一个任务执行助手，拥有以下工具：

                %s

                当收到一个任务时，请按以下流程执行：

                1. 【分析】理解任务需求，明确需要做什么
                2. 【规划】将任务拆解为具体的执行步骤
                3. 【确认】将计划展示给用户，**等待用户确认**。
                   用户明确说"执行"、"确认"、"开始"之后，再进行第4步。
                4. 【执行】使用工具逐步执行，每步说明正在做什么
                5. 【观察】工具返回结果后判断是否符合预期
                6. 【汇总】任务完成后给出最终结果

                请展示你的思考过程，让用户看到每一步的进展。
                如果是普通对话，不需要使用工具时直接回答即可。
                """.formatted(toolList);

        return ChatClient.builder(chatModel)
                .defaultSystem(systemPrompt)
                .defaultToolCallbacks(toolCallbackProvider)
                .build();
    }
}