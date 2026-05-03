package com.ai.mcp.client.entity;

import java.util.List;

public record TaskPlan(String taskName, List<String> steps, int estimatedMinutes) {
}
