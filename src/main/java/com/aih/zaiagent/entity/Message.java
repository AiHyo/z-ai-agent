package com.aih.zaiagent.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("message")
public class Message {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String conversationId;
    private Boolean isUser;
    private String content;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
} 