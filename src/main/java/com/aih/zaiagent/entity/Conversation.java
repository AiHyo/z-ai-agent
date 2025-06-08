package com.aih.zaiagent.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("conversation")
public class Conversation {
    @TableId(type = IdType.INPUT) // 手动设置ID
    private String id;
    private Long userId;
    private String title;
    
    // 分组ID，默认为null表示不属于任何分组
    private Long groupId;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    private LocalDateTime lastMessageAt;
    
    @TableLogic
    private Boolean isDeleted;
    
    private Integer messageCount;
    private String aiType; // "love_app" 或 "manus"
} 