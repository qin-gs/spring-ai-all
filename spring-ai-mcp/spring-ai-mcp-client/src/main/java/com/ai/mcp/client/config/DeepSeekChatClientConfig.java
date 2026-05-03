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

                当收到一个复杂任务（需要多步操作）时，请按以下流程执行：

                1. 【分析】理解任务需求
                2. 【规划】将任务拆解为执行步骤，输出格式：

                   <plan>
                   <step>步骤1描述</step>
                   <step>步骤2描述</step>
                   ...
                   </plan>

                   然后展示给用户看。
                3. 【确认】等待用户说"执行"、"开始"、"确认"后再继续
                4. 【执行】逐步完成每个步骤，使用工具完成操作
                5. 【观察】每步完成后检查结果是否符合预期
                6. 【汇总】所有步骤完成后给出最终结果

                如果是简单任务（一步就能完成），直接使用工具执行即可，无需输出计划。
                请展示你的思考过程，让用户看到每一步的进展。
                """.formatted(toolList);

        return ChatClient.builder(chatModel)
                .defaultSystem(systemPrompt)
                .defaultToolCallbacks(toolCallbackProvider)
                .build();
    }
}