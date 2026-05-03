package com.ai.mcp.client.memory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Repository
public class FileChatMemoryRepository implements ChatMemoryRepository {

    private final Path sessionDir;
    private final ObjectMapper objectMapper;

    public FileChatMemoryRepository(@Value("${app.session.store-path:./sessions}") String storePath) throws IOException {
        this.sessionDir = Paths.get(storePath).toAbsolutePath().normalize();
        this.objectMapper = new ObjectMapper();
        Files.createDirectories(sessionDir);
    }

    @Override
    public List<String> findConversationIds() {
        try (Stream<Path> paths = Files.list(sessionDir)) {
            return paths.filter(p -> p.toString().endsWith(".json"))
                    .map(p -> p.getFileName().toString().replace(".json", ""))
                    .sorted()
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return List.of();
        }
    }

    @Override
    public List<Message> findByConversationId(String conversationId) {
        Path file = sessionDir.resolve(conversationId + ".json");
        if (!Files.exists(file)) {
            return List.of();
        }
        try {
            List<MessageRecord> records = objectMapper.readValue(file.toFile(),
                    new TypeReference<List<MessageRecord>>() {});
            return records.stream()
                    .map(MessageRecord::toMessage)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return List.of();
        }
    }

    @Override
    public void saveAll(String conversationId, List<Message> messages) {
        Path file = sessionDir.resolve(conversationId + ".json");
        try {
            List<MessageRecord> records = messages.stream()
                    .map(MessageRecord::fromMessage)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), records);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save session: " + conversationId, e);
        }
    }

    @Override
    public void deleteByConversationId(String conversationId) {
        Path file = sessionDir.resolve(conversationId + ".json");
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            // ignore
        }
    }

    // ---- JSON serialization helper ----
    // Persists only USER and ASSISTANT messages.
    // TOOL messages (intermediate results) are excluded to keep the history concise.

    private static class MessageRecord {
        public String type;
        public String text;

        static MessageRecord fromMessage(Message msg) {
            if (msg.getMessageType() == MessageType.TOOL
                    || msg.getMessageType() == MessageType.SYSTEM) {
                return null;
            }
            MessageRecord record = new MessageRecord();
            record.type = msg.getMessageType().name();
            record.text = msg.getText();
            return record;
        }

        Message toMessage() {
            String content = text != null ? text : "";
            return switch (MessageType.valueOf(type)) {
                case USER -> new UserMessage(content);
                case ASSISTANT -> new AssistantMessage(content);
                default -> null;
            };
        }
    }
}
