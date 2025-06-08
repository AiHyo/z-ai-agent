package com.aih.zaiagent.service;

import com.aih.zaiagent.dto.ConversationGroupRequest;
import com.aih.zaiagent.entity.ConversationGroup;

import java.util.List;

/**
 * 会话分组服务接口
 */
public interface ConversationGroupService {
    /**
     * 创建会话分组
     * 
     * @param userId 用户ID
     * @param request 分组请求
     * @return 创建的分组
     */
    ConversationGroup createGroup(Long userId, ConversationGroupRequest request);
    
    /**
     * 获取用户的所有会话分组
     * 
     * @param userId 用户ID
     * @return 分组列表
     */
    List<ConversationGroup> getUserGroups(Long userId);
    
    /**
     * 获取用户的特定AI类型的会话分组
     *
     * @param userId 用户ID
     * @param aiType AI类型
     * @return 分组列表
     */
    List<ConversationGroup> getUserGroupsByAiType(Long userId, String aiType);
    
    /**
     * 更新会话分组
     * 
     * @param userId 用户ID
     * @param request 分组请求
     * @return 更新结果
     */
    ConversationGroup updateGroup(Long userId, ConversationGroupRequest request);
    
    /**
     * 删除会话分组
     * 
     * @param userId 用户ID
     * @param groupId 分组ID
     * @return 删除结果
     */
    Boolean deleteGroup(Long userId, Long groupId);
} 