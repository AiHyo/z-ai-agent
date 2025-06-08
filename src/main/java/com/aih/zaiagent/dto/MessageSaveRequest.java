package com.aih.zaiagent.dto;

import lombok.Data;

/**
 * 消息保存请求DTO
 */
@Data
public class MessageSaveRequest {
    /**
     * 会话ID
     */
    private String conversationId;
    
    /**
     * 用户消息内容
     */
    private String userMessage;
    
    /**
     * AI回复内容
     */
    private String aiResponse;
    
    /**
     * AI类型
     */
    private String aiType;
} 