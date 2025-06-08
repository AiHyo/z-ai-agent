package com.aih.zaiagent.controller;

import com.aih.zaiagent.common.BaseResponse;
import com.aih.zaiagent.common.ResultUtils;
import com.aih.zaiagent.dto.ConversationGroupRequest;
import com.aih.zaiagent.entity.ConversationGroup;
import com.aih.zaiagent.service.ConversationGroupService;
import com.aih.zaiagent.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 会话分组控制器
 */
@RestController
@RequestMapping("/conversation/group")
@RequiredArgsConstructor
public class ConversationGroupController {

    private final ConversationGroupService conversationGroupService;
    private final UserService userService;

    /**
     * 创建会话分组
     */
    @PostMapping("/create")
    public BaseResponse<ConversationGroup> createGroup(@RequestBody ConversationGroupRequest request) {
        Long userId = userService.getCurrentUser().getId();
        ConversationGroup group = conversationGroupService.createGroup(userId, request);
        return ResultUtils.success(group);
    }

    /**
     * 获取用户的所有会话分组
     * 可选参数aiType用于按AI类型过滤
     */
    @GetMapping("/list")
    public BaseResponse<List<ConversationGroup>> listGroups(@RequestParam(required = false) String aiType) {
        Long userId = userService.getCurrentUser().getId();
        List<ConversationGroup> groups;
        
        // 如果指定了aiType，按类型过滤
        if (aiType != null && !aiType.trim().isEmpty()) {
            groups = conversationGroupService.getUserGroupsByAiType(userId, aiType);
        } else {
            groups = conversationGroupService.getUserGroups(userId);
        }
        
        return ResultUtils.success(groups);
    }

    /**
     * 更新会话分组
     */
    @PostMapping("/update")
    public BaseResponse<ConversationGroup> updateGroup(@RequestBody ConversationGroupRequest request) {
        Long userId = userService.getCurrentUser().getId();
        ConversationGroup group = conversationGroupService.updateGroup(userId, request);
        return ResultUtils.success(group);
    }

    /**
     * 删除会话分组
     */
    @DeleteMapping("/{groupId}")
    public BaseResponse<Boolean> deleteGroup(@PathVariable Long groupId) {
        Long userId = userService.getCurrentUser().getId();
        Boolean result = conversationGroupService.deleteGroup(userId, groupId);
        return ResultUtils.success(result);
    }
}
