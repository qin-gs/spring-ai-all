package com.ai.mcp.client.controller;

import com.ai.mcp.client.entity.CodeReview;
import com.ai.mcp.client.entity.TaskPlan;
import jakarta.servlet.http.HttpSession;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.StructuredOutputValidationAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("mcp")
public class TaskController {

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private ChatMemory chatMemory;

    @GetMapping(value = "task")
    public String executeTask(
            @RequestParam String description,
            HttpSession session) {

        return chatClient.prompt()
                .advisors(MessageChatMemoryAdvisor.builder(chatMemory)
                        .conversationId("task-" + session.getId())
                        .build())
                .user(description)
                .call()
                .content();
    }

    @GetMapping(value = "stream", produces = "text/event-stream;charset=utf-8")
    public Flux<String> executeTaskByStream(
            @RequestParam String description,
            HttpSession session) {

        return chatClient.prompt()
                .advisors(MessageChatMemoryAdvisor.builder(chatMemory)
                        .conversationId("task-" + session.getId())
                        .build())
                .user(description)
                .stream()
                .content();
    }

    /**
     * 使用 StructuredOutputValidationAdvisor 的递归校验演示。
     * <p>
     * 工作流程：
     * 1. Advisor 根据 TaskPlan.class 自动生成 JSON Schema 并附加到提示中
     * 2. LLM 返回 JSON 响应
     * 3. Advisor 校验 JSON 是否符合 Schema
     * 4. 符合 → .entity() 反序列化为 TaskPlan 对象返回
     * 5. 不符合 → 将校验错误附加到提示，递归重新调用 LLM（最多 3 次）
     * <p>
     * 这就是 Recursive Advisor 的核心模式：在条件满足前反复循环执行下游链。
     */
    @GetMapping("plan")
    public TaskPlan createPlan(@RequestParam String description) {
        return chatClient.prompt()
                .user(description)
                .advisors(StructuredOutputValidationAdvisor.builder()
                        .outputType(TaskPlan.class)
                        .maxRepeatAttempts(3)
                        .build())
                .call()
                .entity(TaskPlan.class);
    }

    /**
     * 使用 .entity() 直接做结构化输出，由 BeanOutputConverter 内部处理 JSON 解析。
     * 不需要显式指定 Advisor，适合对输出质量要求不高的场景。
     * <p>
     * 原理：
     * 调用时 — 把类型描述注入提示 从 POJO 的字段类型生成 JSON Schema，然后通过 getFormat() 返回一段文本，自动拼接到 system prompt 里
     * 相当于告诉 LLM："你必须按这个 JSON 格式返回"。
     * <p>
     * 模型直接返回指定格式，不是 springai 进行的转换
     */
    @GetMapping("review")
    public CodeReview reviewCode(@RequestParam String codeSnippet) {
        return chatClient.prompt()
                .user("请审查以下代码，给出评分、问题列表和改进建议：\n" + codeSnippet)
                .call()
                .entity(CodeReview.class);
    }
}
