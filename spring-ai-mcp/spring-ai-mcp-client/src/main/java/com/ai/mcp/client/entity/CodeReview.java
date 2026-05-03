package com.ai.mcp.client.entity;

import java.util.List;

public record CodeReview(String file, String rating, List<String> issues, List<String> suggestions) {
}
