package com.aih.zaiagent.service;

import com.aih.zaiagent.entity.User;
import com.aih.zaiagent.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import cn.hutool.crypto.digest.BCrypt;
import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserMapper userMapper;

    /**
     * 注册新用户
     */
    public User register(String username, String password, String nickname) {
        // 检查用户名是否已存在
        if (isUsernameExists(username)) {
            throw new RuntimeException("用户名已存在");
        }

        User user = new User();
        user.setUsername(username);
        // 使用Hutool的BCrypt加密
        user.setPassword(BCrypt.hashpw(password));
        user.setNickname(nickname);

        userMapper.insert(user);
        return user;
    }

    /**
     * 用户登录
     */
    public Map<String, Object> login(String username, String password) {
        User user = getUserByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 使用Hutool的BCrypt验证
        if (!BCrypt.checkpw(password, user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        // 更新最后登录时间
        user.setLastLoginAt(LocalDateTime.now());
        userMapper.updateById(user);

        // 登录 (使用默认的 token 生成方式)
        StpUtil.login(user.getId());
        String tokenValue = StpUtil.getTokenValue();

        Map<String, Object> result = new HashMap<>();
        // 统一使用Authorization作为token名称
        result.put("Authorization", tokenValue);
        result.put("user", user);

        return result;
    }

    /**
     * 退出登录
     */
    public void logout() {
        StpUtil.logout();
    }

    /**
     * 获取当前登录用户
     */
    public User getCurrentUser() {
        Long userId = StpUtil.getLoginIdAsLong();
        return getUserById(userId);
    }

    /**
     * 通过用户名查找用户
     */
    public User getUserByUsername(String username) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);
        return userMapper.selectOne(queryWrapper);
    }

    /**
     * 通过ID查找用户
     */
    public User getUserById(Long id) {
        return userMapper.selectById(id);
    }

    /**
     * 检查用户名是否已存在
     */
    public boolean isUsernameExists(String username) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);
        return userMapper.selectCount(queryWrapper) > 0;
    }
}
