package com.aih.zaiagent.dto;

import lombok.Data;

/**
 * 会话更新请求DTO
 */
@Data
public class ConversationUpdateRequest {
    /**
     * 会话ID
     */
    private String id;
    
    /**
     * 会话标题
     */
    private String title;
    
    /**
     * 分组ID，null表示不属于任何分组
     */
    private Long groupId;
} 