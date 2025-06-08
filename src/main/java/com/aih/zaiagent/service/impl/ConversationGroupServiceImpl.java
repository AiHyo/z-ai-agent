package com.aih.zaiagent.service.impl;

import com.aih.zaiagent.dto.ConversationGroupRequest;
import com.aih.zaiagent.entity.ConversationGroup;
import com.aih.zaiagent.exception.BusinessException;
import com.aih.zaiagent.exception.ErrorCode;
import com.aih.zaiagent.mapper.ConversationGroupMapper;
import com.aih.zaiagent.mapper.ConversationMapper;
import com.aih.zaiagent.service.ConversationGroupService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 会话分组服务实现类
 */
@Service
@RequiredArgsConstructor
public class ConversationGroupServiceImpl implements ConversationGroupService {
    private final ConversationGroupMapper conversationGroupMapper;
    private final ConversationMapper conversationMapper;
    
    @Override
    public ConversationGroup createGroup(Long userId, ConversationGroupRequest request) {
        if (request == null || request.getName() == null || request.getName().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "分组名称不能为空");
        }
        
        ConversationGroup group = new ConversationGroup();
        group.setName(request.getName().trim());
        group.setUserId(userId);
        // 设置AI类型
        group.setAiType(request.getAiType());
        
        conversationGroupMapper.insert(group);
        return group;
    }
    
    @Override
    public List<ConversationGroup> getUserGroups(Long userId) {
        LambdaQueryWrapper<ConversationGroup> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ConversationGroup::getUserId, userId)
                .orderByDesc(ConversationGroup::getCreatedAt);
        
        return conversationGroupMapper.selectList(queryWrapper);
    }
    
    @Override
    public List<ConversationGroup> getUserGroupsByAiType(Long userId, String aiType) {
        LambdaQueryWrapper<ConversationGroup> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ConversationGroup::getUserId, userId);
        
        // 如果提供了AI类型，按类型过滤
        if (aiType != null && !aiType.trim().isEmpty()) {
            queryWrapper.eq(ConversationGroup::getAiType, aiType);
        }
        
        queryWrapper.orderByDesc(ConversationGroup::getCreatedAt);
        
        return conversationGroupMapper.selectList(queryWrapper);
    }
    
    @Override
    public ConversationGroup updateGroup(Long userId, ConversationGroupRequest request) {
        if (request == null || request.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "分组ID不能为空");
        }
        
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "分组名称不能为空");
        }
        
        // 检查分组是否存在且属于当前用户
        ConversationGroup group = conversationGroupMapper.selectById(request.getId());
        if (group == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "分组不存在");
        }
        
        if (!group.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权操作此分组");
        }
        
        // 更新分组名称
        group.setName(request.getName().trim());
        // 如果提供了AI类型，也更新它
        if (request.getAiType() != null) {
            group.setAiType(request.getAiType());
        }
        conversationGroupMapper.updateById(group);
        
        return group;
    }
    
    @Override
    @Transactional
    public Boolean deleteGroup(Long userId, Long groupId) {
        // 检查分组是否存在且属于当前用户
        ConversationGroup group = conversationGroupMapper.selectById(groupId);
        if (group == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "分组不存在");
        }
        
        if (!group.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权操作此分组");
        }
        
        // 将该分组下的所有会话的分组ID设为null
        LambdaUpdateWrapper<com.aih.zaiagent.entity.Conversation> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(com.aih.zaiagent.entity.Conversation::getGroupId, groupId)
                    .set(com.aih.zaiagent.entity.Conversation::getGroupId, null);
        conversationMapper.update(null, updateWrapper);
        
        // 删除分组
        conversationGroupMapper.deleteById(groupId);
        
        return true;
    }
} 