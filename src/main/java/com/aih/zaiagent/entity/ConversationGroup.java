package com.aih.zaiagent.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 会话分组实体类
 */
@Data
@TableName("conversation_group")
public class ConversationGroup implements Serializable {

    /**
     * 分组ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 分组名称
     */
    private String name;

    /**
     * 所属用户ID
     */
    private Long userId;

    /**
     * AI类型，用于区分不同聊天室的分组
     * 例如："love-app" 或 "manus"
     */
    private String aiType;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 是否删除
     */
    @TableLogic
    private Boolean isDeleted;
} 