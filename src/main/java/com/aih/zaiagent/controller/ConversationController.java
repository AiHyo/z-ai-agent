package com.aih.zaiagent.controller;

import com.aih.zaiagent.common.BaseResponse;
import com.aih.zaiagent.common.ResultUtils;
import com.aih.zaiagent.dto.ConversationUpdateRequest;
import com.aih.zaiagent.entity.Conversation;
import com.aih.zaiagent.entity.Message;
import com.aih.zaiagent.exception.BusinessException;
import com.aih.zaiagent.exception.ErrorCode;
import com.aih.zaiagent.service.ConversationService;
import com.aih.zaiagent.service.UserService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/conversation")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;
    private final UserService userService;

    /**
     * 创建新会话
     */
    @PostMapping("/create")
    public BaseResponse<Conversation> create(@RequestParam String aiType,
                           @RequestParam(required = false) String firstMessage,
                           @RequestParam(required = false) Long groupId) {
        Long userId = userService.getCurrentUser().getId();

        Conversation conversation;
        if (groupId != null) {
            conversation = conversationService.createConversation(userId, aiType, firstMessage, groupId);
        } else {
            conversation = conversationService.createConversation(userId, aiType, firstMessage);
        }

        return ResultUtils.success(conversation);
    }

    /**
     * 获取用户会话列表
     */
    @GetMapping("/list")
    public BaseResponse<Map<String, Object>> list(@RequestParam(defaultValue = "1") int page,
                         @RequestParam(defaultValue = "10") int size,
                         @RequestParam(required = false) Long groupId,
                         @RequestParam(required = false) String aiType) {
        Long userId = userService.getCurrentUser().getId();
        Page<Conversation> conversations;

        // 根据分组ID和AI类型获取不同的会话列表
        if (aiType != null && !aiType.isEmpty()) {
            // 如果指定了AI类型
            if (groupId != null && groupId > 0) {
                // 按分组和AI类型过滤，需要在内存中过滤
                conversations = conversationService.getUserConversationsByGroup(userId, groupId, page, size);
                // 在内存中过滤aiType
                conversations.setRecords(
                    conversations.getRecords().stream()
                        .filter(conv -> aiType.equals(conv.getAiType()))
                        .toList()
                );
            } else if (groupId != null && groupId == 0) {
                // 获取未分组但特定AI类型的会话
                conversations = conversationService.getUserUngroupedConversationsByAiType(userId, aiType, page, size);
            } else {
                // 仅按AI类型过滤
                conversations = conversationService.getUserConversationsByAiType(userId, aiType, page, size);
            }
        } else {
            // 不按AI类型过滤，只按分组过滤
            if (groupId != null && groupId > 0) {
                conversations = conversationService.getUserConversationsByGroup(userId, groupId, page, size);
            } else if (groupId != null && groupId == 0) {
                // 约定groupId=0表示获取未分组的会话
                conversations = conversationService.getUserUngroupedConversations(userId, page, size);
            } else {
                // 获取所有会话
                conversations = conversationService.getUserConversations(userId, page, size);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("records", conversations.getRecords());
        result.put("total", conversations.getTotal());
        result.put("pages", conversations.getPages());
        result.put("current", conversations.getCurrent());
        result.put("size", conversations.getSize());

        return ResultUtils.success(result);
    }

    /**
     * 获取会话消息历史
     */
    @GetMapping("/{conversationId}/messages")
    public BaseResponse<Map<String, Object>> messages(@PathVariable String conversationId) {
        // 权限检查
        Long userId = userService.getCurrentUser().getId();

        // 获取会话信息
        Conversation conversation = conversationService.getConversationById(conversationId);

        if (!conversation.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权访问此会话");
        }

        // 获取消息列表
        List<Message> messages = conversationService.getConversationMessages(conversationId);

        Map<String, Object> result = new HashMap<>();
        result.put("conversation", conversation);
        result.put("messages", messages);

        return ResultUtils.success(result);
    }

    /**
     * 更新会话信息（标题和分组）
     */
    @PostMapping("/update")
    public BaseResponse<Conversation> updateConversation(@RequestBody ConversationUpdateRequest request) {
        Long userId = userService.getCurrentUser().getId();
        Conversation conversation = conversationService.updateConversation(userId, request);
        return ResultUtils.success(conversation);
    }

    /**
     * 删除会话
     */
    @DeleteMapping("/{conversationId}")
    public BaseResponse<Boolean> delete(@PathVariable String conversationId) {
        Long userId = userService.getCurrentUser().getId();
        Boolean result = conversationService.deleteConversation(conversationId, userId);
        return ResultUtils.success(result);
    }
}
