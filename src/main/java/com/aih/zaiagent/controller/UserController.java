package com.aih.zaiagent.controller;

import com.aih.zaiagent.common.BaseResponse;
import com.aih.zaiagent.common.ResultUtils;
import com.aih.zaiagent.dto.UserLoginRequest;
import com.aih.zaiagent.dto.UserRegisterRequest;
import com.aih.zaiagent.entity.User;
import com.aih.zaiagent.exception.BusinessException;
import com.aih.zaiagent.exception.ErrorCode;
import com.aih.zaiagent.service.UserService;
import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 注册接口
     */
    @PostMapping("/register")
    public BaseResponse<User> register(@RequestBody UserRegisterRequest request) {
        User user = userService.register(request.getUsername(), request.getPassword(), 
                request.getNickname() != null ? request.getNickname() : request.getUsername());
        return ResultUtils.success(user);
    }

    /**
     * 登录接口
     */
    @PostMapping("/login")
    public BaseResponse<Map<String, Object>> login(@RequestBody UserLoginRequest request) {
        log.info("用户登录请求，用户名: {}", request.getUsername());
        Map<String, Object> loginResult = userService.login(request.getUsername(), request.getPassword());
        return ResultUtils.success(loginResult);
    }

    /**
     * 退出登录接口
     */
    @PostMapping("/logout")
    public BaseResponse<String> logout() {
        userService.logout();
        return ResultUtils.success("退出成功");
    }

    /**
     * 判断是否登录
     */
    @GetMapping("/isLogin")
    public BaseResponse<Boolean> isLogin() {
        return ResultUtils.success(StpUtil.isLogin());
    }

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/info")
    public BaseResponse<User> userInfo() {
        if (!StpUtil.isLogin()) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        User currentUser = userService.getCurrentUser();
        return ResultUtils.success(currentUser);
    }
}
