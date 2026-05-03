package com.ai.mcp.client.handler;

import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.mcp.annotation.McpLogging;
import org.springaicommunity.mcp.annotation.McpToolListChanged;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class McpClientHandlers {

    private static final Logger log = LoggerFactory.getLogger(McpClientHandlers.class);

    @McpLogging(clients = {"general-mcp-server"})
    public void handleLogging(McpSchema.LoggingMessageNotification notification) {
        log.info("[MCP 服务端日志] 级别={}, 来源={}, 消息={}", notification.level(), notification.logger(), notification.data());
    }

    @McpToolListChanged(clients = {"general-mcp-server"})
    public void handleToolListChanged(List<McpSchema.Tool> tools) {
        log.info("[MCP 工具列表已更新] 当前有 {} 个工具:", tools.size());
        tools.forEach(tool -> log.info("  - {}: {}", tool.name(), tool.description()));
    }
}
