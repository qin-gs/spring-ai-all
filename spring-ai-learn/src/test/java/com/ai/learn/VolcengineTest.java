// package com.ai.learn;
//
// import com.ai.learn.entity.DoubaoEntity;
// import lombok.extern.slf4j.Slf4j;
// import org.junit.Test;
// import org.springframework.http.*;
// import org.springframework.web.client.RestTemplate;
//
// import java.util.ArrayList;
// import java.util.HashMap;
// import java.util.List;
// import java.util.Map;
//
// @Slf4j
// public class VolcengineTest {
//
//     @Test
//     public void test() {
//
//         System.out.println("Hello world");
//     }
//
//     public static void main(String[] args) {
//         RestTemplate restTemplate = new RestTemplate();
//
//         // 构建请求头
//         HttpHeaders headers = new HttpHeaders();
//         headers.setContentType(MediaType.APPLICATION_JSON);
//         headers.set("Authorization", "Bearer 01f2ad94-8762-4e7c-9e74-ab7855bb22db");
//
//         // 构建请求体
//         Map<String, Object> requestBody = new HashMap<>();
//         requestBody.put("model", "doubao-1-5-thinking-pro-250415");
//
//         List<Map<String, String>> messages = new ArrayList<>();
//         messages.add(createMessage("system", "你是人工智能助手."));
//         messages.add(createMessage("user", "给我讲一个笑话"));
//         requestBody.put("messages", messages);
//
//         // 创建请求实体
//         HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
//
//         // 发送POST请求
//         ResponseEntity<DoubaoEntity> response = restTemplate.exchange(
//                 "https://ark.cn-beijing.volces.com/api/v3/chat/completions",
//                 HttpMethod.POST,
//                 requestEntity,
//                 DoubaoEntity.class
//         );
//         System.out.println("响应状态码: " + response.getStatusCode());
//         System.out.println("响应体: " + response.getBody());
//     }
//
//     private static Map<String, String> createMessage(String role, String content) {
//         Map<String, String> message = new HashMap<>();
//         message.put("role", role);
//         message.put("content", content);
//         return message;
//     }
//
// }
