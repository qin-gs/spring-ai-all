package com.ai.mcp.server.config;

import com.ai.mcp.server.service.DateTimeServiceServer;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpServerConfiguration {

    @Bean
    public ToolCallbackProvider toolCallbackProvider(DateTimeServiceServer dateTimeServiceServer) {
        return MethodToolCallbackProvider.builder().toolObjects(dateTimeServiceServer).build();
    }
}
