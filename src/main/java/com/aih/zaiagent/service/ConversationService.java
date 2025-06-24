package com.aih.zaiagent.service;

import com.aih.zaiagent.dto.ConversationUpdateRequest;
import com.aih.zaiagent.entity.Conversation;
import com.aih.zaiagent.entity.MessageDB;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 会话服务接口
 */
public interface ConversationService extends IService<Conversation> {

    /**
     * 创建会话
     */
    Conversation createConversation(Long userId, String aiType, String firstMessage);

    /**
     * 创建会话（带分组）
     */
    Conversation createConversation(Long userId, String aiType, String firstMessage, Long groupId);

    /**
     * 获取会话详情
     */
    Conversation getConversationById(String conversationId);

    /**
     * 获取用户的会话列表
     */
    Page<Conversation> getUserConversations(Long userId, int page, int size);

    /**
     * 获取用户特定AI类型的会话列表
     */
    Page<Conversation> getUserConversationsByAiType(Long userId, String aiType, int page, int size);

    /**
     * 获取用户某个分组下的会话列表
     */
    Page<Conversation> getUserConversationsByGroup(Long userId, Long groupId, int page, int size);

    /**
     * 获取用户未分组的会话列表
     */
    Page<Conversation> getUserUngroupedConversations(Long userId, int page, int size);

    /**
     * 获取用户特定AI类型的未分组会话列表
     */
    Page<Conversation> getUserUngroupedConversationsByAiType(Long userId, String aiType, int page, int size);

    /**
     * 更新会话信息
     */
    Conversation updateConversation(Long userId, ConversationUpdateRequest request);

    /**
     * 删除会话
     */
    Boolean deleteConversation(String conversationId, Long userId);

    /**
     * 获取会话的消息列表
     */
    List<MessageDB> getConversationMessages(String conversationId);

    /**
     * 检查用户是否为会话所有者
     */
    Boolean isConversationOwner(String conversationId, Long userId);

    /**
     * 保存消息
     */
    MessageDB saveMessage(String conversationId, Long userId, String content, String role);

    /**
     * 更新会话的最后一条消息
     */
    void updateLastMessage(String conversationId, String lastMessage);

    void mergeAiMessageByConversationId(String conversationId);
}
