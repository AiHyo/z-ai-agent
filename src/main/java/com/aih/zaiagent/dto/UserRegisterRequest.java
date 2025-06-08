package com.aih.zaiagent.dto;

import lombok.Data;

/**
 * 用户注册请求DTO
 */
@Data
public class UserRegisterRequest {
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 密码
     */
    private String password;
    
    /**
     * 昵称，非必填
     */
    private String nickname;
} 