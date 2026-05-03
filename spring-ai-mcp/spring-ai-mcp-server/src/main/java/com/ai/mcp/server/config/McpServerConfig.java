package com.ai.mcp.server.config;

import com.ai.mcp.server.service.DateTimeServiceServer;
import com.ai.mcp.server.service.FileCommandService;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpServerConfig {

    @Bean
    public ToolCallbackProvider toolCallbackProvider(DateTimeServiceServer dateTimeServiceServer,
                                                     FileCommandService fileCommandService) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(dateTimeServiceServer, fileCommandService)
                .build();
    }
}
