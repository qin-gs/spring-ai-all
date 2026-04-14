// package com.ai.learn.entity;
//
// import lombok.Data;
//
// import java.util.List;
//
// @Data
// public class DoubaoEntity {
//     public List<Choice> choices;
//     public Long created;
//     public String id;
//     public String model;
//     public String service_tier;
//     public String object;
//     public Usage usage;
//
//     @Data
//     public static class Choice {
//         public String finish_reason;
//         public Integer index;
//         // 如有具体结构，可替换类型
//         public Object logprobs;
//         public Message message;
//     }
//
//     @Data
//     public static class Message {
//         public String content;
//         public String reasoning_content;
//         public String role;
//     }
//
//     @Data
//     public static class Usage {
//         public Integer completion_tokens;
//         public Integer prompt_tokens;
//         public Integer total_tokens;
//         public PromptTokensDetails prompt_tokens_details;
//         public CompletionTokensDetails completion_tokens_details;
//     }
//
//     @Data
//     public static class PromptTokensDetails {
//         public Integer cached_tokens;
//     }
//
//     @Data
//     public static class CompletionTokensDetails {
//         public Integer reasoning_tokens;
//     }
// }
