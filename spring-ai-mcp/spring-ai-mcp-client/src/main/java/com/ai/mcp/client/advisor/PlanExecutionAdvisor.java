package com.ai.mcp.client.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 自定义 Recursive Advisor：任务计划与逐步执行。
 * <p>
 * 工作方式：
 * <ol>
 *   <li>LLM 输出计划（用 &lt;plan&gt;...&lt;/plan&gt; 包裹步骤）</li>
 *   <li>用户确认后，Advisor 进入执行循环</li>
 *   <li>每步通过 chain.copy(this) 创建子链，修改请求后重新调用 LLM</li>
 *   <li>所有步骤完成后返回最终结果</li>
 * </ol>
 */
@Slf4j
public class PlanExecutionAdvisor implements CallAdvisor, StreamAdvisor {

    private static final Pattern PLAN_PATTERN = Pattern.compile(
            "<plan>\\s*<step>(.*?)</step>\\s*</plan>", Pattern.DOTALL);
    private static final Pattern STEP_TAG_PATTERN = Pattern.compile(
            "<step>(.*?)</step>", Pattern.DOTALL);

    /**
     * 确认执行的关键词
     */
    private static final List<String> CONFIRM_KEYWORDS = List.of(
            "执行", "开始", "确认", "继续", "好", "是",
            "execute", "start", "confirm", "go", "yes");

    private final int advisorOrder;
    private final ConcurrentHashMap<String, PlanState> plans = new ConcurrentHashMap<>();

    public PlanExecutionAdvisor(int advisorOrder) {
        this.advisorOrder = advisorOrder;
    }

    @Override
    public String getName() {
        return "PlanExecutionAdvisor";
    }

    @Override
    public int getOrder() {
        return advisorOrder;
    }

    // ========== 同步调用 ==========

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        String conversationId = resolveConversationId(request);
        PlanState planState = plans.get(conversationId);

        // 如果已有正在执行的计划 → 进入执行循环
        if (planState != null && "EXECUTING".equals(planState.status())) {
            return executeSteps(request, chain, conversationId, planState, 0);
        }

        // 正常调 LLM
        ChatClientResponse response = chain.nextCall(request);

        // 如果之前有计划在等待确认，且用户确认了 → 开始执行
        if (planState != null && "WAITING_CONFIRM".equals(planState.status())) {
            String userText = extractUserText(request);
            if (isConfirm(userText)) {
                log.info("用户确认执行，开始逐步执行计划: conversationId={}", conversationId);
                return executeSteps(request, chain, conversationId, planState, 0);
            }
        }

        // 检查 LLM 是否输出了计划
        String responseText = responseText(response);
        if (responseText != null && responseText.contains("<plan>")) {
            List<String> steps = parseSteps(responseText);
            if (!steps.isEmpty()) {
                plans.put(conversationId, new PlanState(
                        extractUserText(request),
                        steps,
                        "WAITING_CONFIRM"
                ));
                log.info("检测到计划，等待用户确认: {} 个步骤", steps.size());
            }
        }

        return response;
    }

    // ========== 流式调用 ==========

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest request, StreamAdvisorChain chain) {
        // 流式模式下简化为透传
        return chain.nextStream(request);
    }

    // ========== 执行循环 ==========

    private ChatClientResponse executeSteps(ChatClientRequest request, CallAdvisorChain chain,
                                            String conversationId, PlanState planState, int depth) {
        if (depth >= planState.steps().size()) {
            // 所有步骤完成
            plans.put(conversationId, new PlanState(
                    planState.taskDescription(), planState.steps(), "COMPLETED"));
            return chatResponse("✅ 所有步骤执行完成！\n\n" + planState.summary());
        }

        // 标记为执行中
        plans.put(conversationId, new PlanState(
                planState.taskDescription(), planState.steps(), "EXECUTING", planState.stepResults()));

        String currentStep = planState.steps().get(depth);
        log.info("执行步骤 {}/{}: {}", depth + 1, planState.steps().size(), currentStep);

        // 构造带步骤上下文的请求
        ChatClientRequest stepRequest = buildStepRequest(request, planState, depth);

        // 创建子链（跳过当前 advisor）
        CallAdvisorChain subChain = chain.copy(this);

        // 调 LLM
        ChatClientResponse response = subChain.nextCall(stepRequest);

        // 记录执行结果
        String result = response != null ? responseText(response) : "";
        planState = planState.withStepResult(depth, result);

        // 递归执行下一步
        return executeSteps(request, chain, conversationId, planState, depth + 1);
    }

    // ========== 辅助方法 ==========

    private ChatClientRequest buildStepRequest(ChatClientRequest original, PlanState plan, int stepIndex) {
        String stepDescription = plan.steps().get(stepIndex);
        StringBuilder context = new StringBuilder();
        context.append("【任务执行助手 - 逐步执行模式】\n\n");
        context.append("原始任务: ").append(plan.taskDescription()).append("\n\n");
        context.append("总步骤数: ").append(plan.steps().size()).append("\n\n");

        // 已完成步骤
        for (int i = 0; i < stepIndex; i++) {
            context.append("✅ 步骤 ").append(i + 1).append(" 已完成: ")
                    .append(plan.steps().get(i)).append("\n");
            String stepResult = plan.stepResults().get(i);
            if (stepResult != null && !stepResult.isBlank()) {
                context.append("   结果: ").append(
                        stepResult.length() > 200 ? stepResult.substring(0, 200) + "..." : stepResult
                ).append("\n");
            }
        }

        context.append("\n▶ **当前步骤 ").append(stepIndex + 1).append("/")
                .append(plan.steps().size()).append("**: ")
                .append(stepDescription).append("\n");
        context.append("\n请使用适当的工具完成当前步骤。完成后，简要总结该步骤的结果。");

        // 保留原始消息，加上步骤上下文
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(context.toString()));

        Prompt originalPrompt = original.prompt();
        if (originalPrompt != null && originalPrompt.getInstructions() != null) {
            messages.addAll(originalPrompt.getInstructions());
        }

        return original.mutate()
                .prompt(new Prompt(messages))
                .build();
    }

    private String resolveConversationId(ChatClientRequest request) {
        Object id = request.context().get("conversationId");
        return id != null ? id.toString() : "default";
    }

    private String extractUserText(ChatClientRequest request) {
        Prompt prompt = request.prompt();
        if (prompt != null && prompt.getInstructions() != null) {
            for (Message msg : prompt.getInstructions()) {
                if (msg.getMessageType() == org.springframework.ai.chat.messages.MessageType.USER) {
                    return msg.getText();
                }
            }
        }
        return "";
    }

    private List<String> parseSteps(String text) {
        // 先尝试 <plan><step>...</step></plan> 格式
        Matcher planMatcher = PLAN_PATTERN.matcher(text);
        if (planMatcher.find()) {
            Matcher stepMatcher = STEP_TAG_PATTERN.matcher(planMatcher.group(0));
            List<String> steps = new ArrayList<>();
            while (stepMatcher.find()) {
                steps.add(stepMatcher.group(1).trim());
            }
            return steps;
        }
        return List.of();
    }

    private boolean isConfirm(String userText) {
        if (userText == null || userText.isBlank()) return false;
        String trimmed = userText.trim().toLowerCase();
        return CONFIRM_KEYWORDS.stream().anyMatch(kw -> trimmed.contains(kw));
    }

    private static String responseText(ChatClientResponse response) {
        var cr = response.chatResponse();
        if (cr == null) return "";
        var result = cr.getResult();
        if (result == null) return "";
        var output = result.getOutput();
        return output != null ? output.getText() : "";
    }

    private ChatClientResponse chatResponse(String text) {
        var assistantMessage = new org.springframework.ai.chat.messages.AssistantMessage(text);
        var generation = new org.springframework.ai.chat.model.Generation(assistantMessage);
        return ChatClientResponse.builder()
                .chatResponse(new org.springframework.ai.chat.model.ChatResponse(List.of(generation)))
                .build();
    }

    // ========== 内部状态 ==========

    public record PlanState(
            String taskDescription,
            List<String> steps,
            String status,
            List<String> stepResults
    ) {
        public PlanState(String taskDescription, List<String> steps, String status) {
            this(taskDescription, steps, status, new ArrayList<>());
        }

        public PlanState withStepResult(int index, String result) {
            List<String> newResults = new ArrayList<>(stepResults);
            while (newResults.size() <= index) {
                newResults.add("");
            }
            newResults.set(index, result);
            return new PlanState(taskDescription, steps, status, newResults);
        }

        public String summary() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < steps.size(); i++) {
                sb.append("步骤 ").append(i + 1).append(": ").append(steps.get(i)).append("\n");
                if (i < stepResults.size() && stepResults.get(i) != null && !stepResults.get(i).isBlank()) {
                    sb.append("  结果: ").append(
                            stepResults.get(i).lines().findFirst().orElse("")
                    ).append("\n");
                }
            }
            return sb.toString();
        }
    }
}
