package com.ai.mcp.server.service;

import lombok.extern.slf4j.Slf4j;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springaicommunity.mcp.context.McpSyncRequestContext;
import org.springframework.stereotype.Component;

import io.modelcontextprotocol.spec.McpSchema;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

@Slf4j
@Component
public class DayOfWeekService {

    @McpTool(name = "getDayOfWeek", description = "获取指定日期是星期几，不传参数则返回当前日期")
    public McpSchema.CallToolResult getDayOfWeek(
            McpSyncRequestContext context,
            @McpToolParam(description = "日期，格式 yyyy-MM-dd，可选", required = false) String dateStr) {

        LocalDate date;
        StringBuilder debug = new StringBuilder();
        if (dateStr != null && !dateStr.isBlank()) {
            date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            debug.append("收到日期参数: ").append(dateStr);
            context.info("正在查询指定日期: " + dateStr);
        } else {
            date = LocalDate.now();
            debug.append("未传日期，使用当前日期: ").append(date);
            context.debug("未传日期，使用当前日期");
        }

        DayOfWeek dayOfWeek = date.getDayOfWeek();
        String chineseName = dayOfWeek.getDisplayName(TextStyle.FULL, Locale.CHINA);
        String englishName = dayOfWeek.getDisplayName(TextStyle.FULL, Locale.US);

        String result = String.format("%s 是 %s (%s)", date, chineseName, englishName);
        log.info("{} 是 {}", date, chineseName);
        context.info("查询结果: " + chineseName);

        return McpSchema.CallToolResult.builder()
                .content(List.of(
                        new McpSchema.TextContent(result),
                        new McpSchema.TextContent("[调试] " + debug)
                ))
                .build();
    }
}
