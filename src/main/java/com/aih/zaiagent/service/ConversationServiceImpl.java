package com.aih.zaiagent.service;

import com.aih.zaiagent.dto.ConversationUpdateRequest;
import com.aih.zaiagent.entity.Conversation;
import com.aih.zaiagent.entity.Message;
import com.aih.zaiagent.exception.BusinessException;
import com.aih.zaiagent.exception.ErrorCode;
import com.aih.zaiagent.mapper.ConversationMapper;
import com.aih.zaiagent.mapper.MessageMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 会话服务实现类
 */
@Service
@RequiredArgsConstructor
public class ConversationServiceImpl extends ServiceImpl<ConversationMapper, Conversation> implements ConversationService {
    private final ConversationMapper conversationMapper;
    private final MessageMapper messageMapper;
    
    /**
     * 生成会话ID
     */
    private String generateConversationId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
    
    /**
     * 创建新会话
     */
    @Override
    @Transactional
    public Conversation createConversation(Long userId, String aiType, String firstUserMessage) {
        // 创建会话
        Conversation conversation = new Conversation();
        conversation.setId(generateConversationId());
        conversation.setUserId(userId);
        conversation.setAiType(aiType);
        conversation.setMessageCount(0);
        conversation.setLastMessageAt(LocalDateTime.now());
        
        // 如果有初始消息，设置为标题
        if (firstUserMessage != null && !firstUserMessage.isEmpty()) {
            // 消息截断为标题
            conversation.setTitle(firstUserMessage.length() > 50 
                ? firstUserMessage.substring(0, 47) + "..." 
                : firstUserMessage);
        } else {
            conversation.setTitle("新对话");
        }
        
        conversationMapper.insert(conversation);
        
        // 保存第一条用户消息（如果有）
        if (firstUserMessage != null && !firstUserMessage.isEmpty()) {
            addMessageInternal(conversation.getId(), true, firstUserMessage);
        }
        
        return conversation;
    }
    
    /**
     * 创建新会话并指定分组
     */
    @Override
    @Transactional
    public Conversation createConversation(Long userId, String aiType, String firstUserMessage, Long groupId) {
        // 创建会话
        Conversation conversation = new Conversation();
        conversation.setId(generateConversationId());
        conversation.setUserId(userId);
        conversation.setAiType(aiType);
        conversation.setMessageCount(0);
        conversation.setLastMessageAt(LocalDateTime.now());
        conversation.setGroupId(groupId);
        
        // 如果有初始消息，设置为标题
        if (firstUserMessage != null && !firstUserMessage.isEmpty()) {
            // 消息截断为标题
            conversation.setTitle(firstUserMessage.length() > 50 
                ? firstUserMessage.substring(0, 47) + "..." 
                : firstUserMessage);
        } else {
            conversation.setTitle("新对话");
        }
        
        conversationMapper.insert(conversation);
        
        // 保存第一条用户消息（如果有）
        if (firstUserMessage != null && !firstUserMessage.isEmpty()) {
            addMessageInternal(conversation.getId(), true, firstUserMessage);
        }
        
        return conversation;
    }
    
    /**
     * 内部使用，添加消息到会话（不包装）
     */
    private Message addMessageInternal(String conversationId, boolean isUser, String content) {
        // 获取会话
        Conversation conversation = getConversationByIdInternal(conversationId);
        if (conversation == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "会话不存在");
        }
        
        // 更新会话的最后消息时间和消息计数
        conversation.setLastMessageAt(LocalDateTime.now());
        conversation.setMessageCount(conversation.getMessageCount() + 1);
        conversationMapper.updateById(conversation);
        
        // 创建消息
        Message message = new Message();
        message.setConversationId(conversationId);
        message.setIsUser(isUser);
        message.setContent(content);
        
        messageMapper.insert(message);
        return message;
    }
    
    /**
     * 添加消息到会话
     */
    @Transactional
    public Message addMessage(String conversationId, boolean isUser, String content) {
        return addMessageInternal(conversationId, isUser, content);
    }
    
    /**
     * 获取用户的所有会话（分页）
     */
    @Override
    public Page<Conversation> getUserConversations(Long userId, int page, int size) {
        Page<Conversation> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Conversation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Conversation::getUserId, userId)
                .orderByDesc(Conversation::getLastMessageAt);
        
        return conversationMapper.selectPage(pageParam, queryWrapper);
    }
    
    /**
     * 获取用户特定AI类型的会话列表（分页）
     */
    @Override
    public Page<Conversation> getUserConversationsByAiType(Long userId, String aiType, int page, int size) {
        Page<Conversation> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Conversation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Conversation::getUserId, userId)
                .eq(Conversation::getAiType, aiType)
                .orderByDesc(Conversation::getLastMessageAt);
        
        return conversationMapper.selectPage(pageParam, queryWrapper);
    }
    
    /**
     * 获取用户指定分组下的所有会话（分页）
     */
    @Override
    public Page<Conversation> getUserConversationsByGroup(Long userId, Long groupId, int page, int size) {
        Page<Conversation> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Conversation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Conversation::getUserId, userId)
                .eq(Conversation::getGroupId, groupId)
                .orderByDesc(Conversation::getLastMessageAt);
        
        return conversationMapper.selectPage(pageParam, queryWrapper);
    }
    
    /**
     * 获取用户未分组的所有会话（分页）
     */
    @Override
    public Page<Conversation> getUserUngroupedConversations(Long userId, int page, int size) {
        Page<Conversation> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Conversation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Conversation::getUserId, userId)
                .isNull(Conversation::getGroupId)
                .orderByDesc(Conversation::getLastMessageAt);
        
        return conversationMapper.selectPage(pageParam, queryWrapper);
    }
    
    /**
     * 获取用户特定AI类型的未分组会话列表（分页）
     */
    @Override
    public Page<Conversation> getUserUngroupedConversationsByAiType(Long userId, String aiType, int page, int size) {
        Page<Conversation> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Conversation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Conversation::getUserId, userId)
                .eq(Conversation::getAiType, aiType)
                .isNull(Conversation::getGroupId)
                .orderByDesc(Conversation::getLastMessageAt);
        
        return conversationMapper.selectPage(pageParam, queryWrapper);
    }
    
    /**
     * 获取会话详情
     */
    @Override
    public Conversation getConversationById(String conversationId) {
        Conversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "会话不存在");
        }
        return conversation;
    }
    
    /**
     * 内部使用，获取会话详情（不包装）
     */
    private Conversation getConversationByIdInternal(String conversationId) {
        return conversationMapper.selectById(conversationId);
    }
    
    /**
     * 获取会话的所有消息
     */
    @Override
    public List<Message> getConversationMessages(String conversationId) {
        LambdaQueryWrapper<Message> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Message::getConversationId, conversationId)
                .orderByAsc(Message::getCreatedAt);
        
        return messageMapper.selectList(queryWrapper);
    }
    
    /**
     * 更新会话信息
     */
    @Override
    @Transactional
    public Conversation updateConversation(Long userId, ConversationUpdateRequest request) {
        // 获取会话
        Conversation conversation = getConversationById(request.getId());
        
        // 检查权限
        if (!conversation.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权修改此会话");
        }
        
        // 更新标题和分组
        if (request.getTitle() != null) {
            conversation.setTitle(request.getTitle());
        }
        
        conversation.setGroupId(request.getGroupId());
        conversationMapper.updateById(conversation);
        
        return conversation;
    }
    
    /**
     * 删除会话
     */
    @Override
    @Transactional
    public Boolean deleteConversation(String conversationId, Long userId) {
        // 获取会话
        Conversation conversation = getConversationById(conversationId);
        
        // 检查权限
        if (!conversation.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权删除此会话");
        }
        
        // 删除会话及相关消息
        conversationMapper.deleteById(conversationId);
        
        LambdaQueryWrapper<Message> messageQueryWrapper = new LambdaQueryWrapper<>();
        messageQueryWrapper.eq(Message::getConversationId, conversationId);
        messageMapper.delete(messageQueryWrapper);
        
        return true;
    }

    @Override
    public Boolean isConversationOwner(String conversationId, Long userId) {
        Conversation conversation = getConversationById(conversationId);
        return conversation.getUserId().equals(userId);
    }

    @Override
    public Message saveMessage(String conversationId, Long userId, String content, String role) {
        return addMessageInternal(conversationId, role.equals("user"), content);
    }

    @Override
    public void updateLastMessage(String conversationId, String lastMessage) {
        Conversation conversation = getConversationById(conversationId);
        conversation.setLastMessageAt(LocalDateTime.now());
        conversation.setTitle(lastMessage.length() > 50 ? lastMessage.substring(0, 47) + "..." : lastMessage);
        conversationMapper.updateById(conversation);
    }
} 