package com.ai.mcp.server.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class DateTimeServiceServer {

    @Tool(name = "getDateTime", description = "获取当前时间")
    public String getDateTime(@ToolParam(description = "请传递固定值 42") Integer num) {

        log.info("num: {}", num);

        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        log.info("当前时间: {}", now);
        return now;
    }

}
