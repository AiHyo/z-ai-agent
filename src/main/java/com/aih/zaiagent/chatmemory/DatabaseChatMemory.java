package com.aih.zaiagent.chatmemory;

import com.aih.zaiagent.entity.MessageDB;
import com.aih.zaiagent.service.ConversationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 基于数据库的聊天记忆实现
 * 直接使用现有的ConversationService和数据模型
 */
@Slf4j
public class DatabaseChatMemory implements ChatMemory {

    private final ConversationService conversationService;
    private final Long userId;

    // 移除userId字段，改为在方法中动态获取

    public DatabaseChatMemory(ConversationService conversationService) {
        this.conversationService = conversationService;
        this.userId = null;
    }

    // 新增带userId的构造函数
    public DatabaseChatMemory(ConversationService conversationService, Long userId) {
        this.conversationService = conversationService;
        this.userId = userId;
    }


    @Override
    public void add(String conversationId, List<Message> messages) {
        log.info("add=>userId{}",userId);

        if (userId == null) {
            log.warn("missing user ID");
            return;
        }

        for (Message message : messages) {
            // 跳过系统消息
            if (!(message instanceof UserMessage || message instanceof AssistantMessage)) {
                continue;
            }

            boolean isUser = message instanceof UserMessage;
            String content = message.getText();

            // 使用现有的ConversationService保存消息
            log.info("conversationId: {}, userId: {}, content: {}, isUser: {}", conversationId, userId, content, isUser);
            conversationService.saveMessage(conversationId, userId, content, isUser ? "user" : "ai");
            log.info("message saved");
        }
    }

    @Override
    public List<Message> get(String conversationId, int lastN) {
        log.info("get=>userId{}",userId);

        if (userId == null) {
            log.warn("missing user ID");
            return null;
        }

        // 使用现有的ConversationService获取消息
        List<MessageDB> dbMessages = conversationService.getConversationMessages(conversationId);
        log.info("dbchatmemory:{}", dbMessages);

        // 将数据库消息转换为Spring AI消息格式
        return dbMessages.stream()
                .skip(Math.max(0, dbMessages.size() - lastN))
                .map(msg -> {
                    if (msg.getIsUser()) {
                        return new UserMessage(msg.getContent());
                    } else {
                        return new AssistantMessage(msg.getContent());
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    public void clear(String conversationId) {
        log.info("clear=>userId{}",userId);

        if (userId == null) {
            log.warn("missing user ID");
            return;
        }
        // 清除会话历史
        conversationService.deleteConversation(conversationId, userId);
    }
}
