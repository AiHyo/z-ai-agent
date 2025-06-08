package com.aih.zaiagent.controller;

import com.aih.zaiagent.common.BaseResponse;
import com.aih.zaiagent.common.ResultUtils;
import com.aih.zaiagent.dto.MessageSaveRequest;
import com.aih.zaiagent.entity.Message;
import com.aih.zaiagent.exception.BusinessException;
import com.aih.zaiagent.exception.ErrorCode;
import com.aih.zaiagent.service.ConversationService;
import com.aih.zaiagent.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 消息控制器
 */
@Slf4j
@RestController
@RequestMapping("/message")
@RequiredArgsConstructor
public class MessageController {

    private final ConversationService conversationService;
    private final UserService userService;

    /**
     * 保存消息（用于流式输出完成后保存）
     */
    @PostMapping("/save")
    public BaseResponse<Message> saveMessage(@RequestBody MessageSaveRequest request) {
        log.info("保存消息请求: conversationId={}", request.getConversationId());
        
        // 权限检查
        Long userId = userService.getCurrentUser().getId();
        
        // 验证会话所属权
        if (!conversationService.isConversationOwner(request.getConversationId(), userId)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权访问此会话");
        }
        
        // 保存用户消息
        Message userMessage = conversationService.saveMessage(
                request.getConversationId(), 
                userId, 
                request.getUserMessage(), 
                "user"
        );
        
        // 保存AI回复
        Message aiMessage = conversationService.saveMessage(
                request.getConversationId(), 
                userId, 
                request.getAiResponse(), 
                "ai"
        );
        
        // 更新会话最后一条消息（用于会话列表展示）
        conversationService.updateLastMessage(request.getConversationId(), request.getAiResponse());
        
        return ResultUtils.success(aiMessage);
    }
    
    /**
     * 获取会话的所有消息
     */
    @GetMapping("/list/{conversationId}")
    public BaseResponse<List<Message>> getMessages(@PathVariable String conversationId) {
        // 权限检查
        Long userId = userService.getCurrentUser().getId();
        
        // 验证会话所属权
        if (!conversationService.isConversationOwner(conversationId, userId)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权访问此会话");
        }
        
        // 获取消息列表
        List<Message> messages = conversationService.getConversationMessages(conversationId);
        
        return ResultUtils.success(messages);
    }
} 