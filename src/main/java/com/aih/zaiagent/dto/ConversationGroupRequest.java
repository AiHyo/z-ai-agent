package com.aih.zaiagent.dto;

import lombok.Data;

/**
 * 会话分组请求DTO
 */
@Data
public class ConversationGroupRequest {
    /**
     * 分组名称
     */
    private String name;
    
    /**
     * 分组ID，用于更新操作
     */
    private Long id;
    
    /**
     * AI类型，用于区分不同聊天室的分组
     * 例如："love-app" 或 "manus"
     */
    private String aiType;
} 