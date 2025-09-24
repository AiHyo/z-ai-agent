package com.aih.zaiagent.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.aih.zaiagent.common.BaseResponse;
import com.aih.zaiagent.common.ResultUtils;
import com.aih.zaiagent.entity.MessageDB;
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
     * 获取会话的所有消息
     */
    @GetMapping("/list/{conversationId}")
    public BaseResponse<List<MessageDB>> getMessages(@PathVariable String conversationId) {
        // 权限检查
        Long userId = StpUtil.getLoginIdAsLong();
        // 验证会话所属权
        if (!conversationService.isConversationOwner(conversationId, userId)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权访问此会话");
        }

        // 获取消息列表
        List<MessageDB> messages = conversationService.getConversationMessages(conversationId);

        return ResultUtils.success(messages);
    }

    @DeleteMapping("/{messageId}")
    public BaseResponse<String> deleteMessage(@PathVariable Long messageId) {
        Long userId = StpUtil.getLoginIdAsLong();
        if (!conversationService.isMessageOwner(messageId, userId)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权删除此消息");
        }
        conversationService.deleteMessage(messageId);
        return ResultUtils.success("删除成功");
    }
}
