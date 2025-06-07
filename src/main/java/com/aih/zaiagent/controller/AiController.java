package com.aih.zaiagent.controller;

import com.aih.zaiagent.agent.MyManus;
import com.aih.zaiagent.app.LoveApp;
import com.aih.zaiagent.entity.Message;
import com.aih.zaiagent.service.ConversationService;
import com.aih.zaiagent.service.UserService;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.Resource;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

/**
 * @author AiHyo
 */
@RestController
@RequestMapping("/api/ai")
public class AiController {

    @Resource
    private LoveApp loveApp;

    @Resource
    private ToolCallback[] availableTools;

    @Resource
    private DashScopeChatModel dashscopeChatModel;
    
    @Resource
    private ConversationService conversationService;
    
    @Resource
    private UserService userService;

    /**
     * 同步调用 LoveApp AI 恋爱大师
     * @param message 用户输入的消息
     * @param conversationId 聊天会话 ID
     * @return 返回聊天结果
     */
    @SaCheckLogin
    @GetMapping("/love_app/chat/sync")
    public String doChatWithLoveAppSync(String message, String conversationId) {
        // 保存用户消息
        saveMessage(conversationId, true, message);
        
        // 调用AI
        String response = loveApp.doChat(message, conversationId);
        
        // 保存AI回复
        saveMessage(conversationId, false, response);
        
        return response;
    }

    /**
     * SSE 流式调用 LoveApp AI 恋爱大师
     * @param message 用户输入的消息
     * @param conversationId 聊天会话 ID
     * @return 返回聊天结果
     */
    // 返回؜ Flux 响应式‌对象，并且添加  SSE 对应的 MediaType
    @GetMapping(value = "/love_app/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatWithLoveAppSse(String message, String conversationId) {
        // 尝试保存用户消息
        try {
            if (StpUtil.isLogin()) {
                saveMessage(conversationId, true, message);
            }
        } catch (Exception e) {
            // 可能未登录，非致命错误，忽略
        }
        
        Flux<String> responseFlux = loveApp.doChatByStream(message, conversationId);
        
        // 在流完成后保存AI回复
        responseFlux = responseFlux.cache(); // 缓存流内容
        
        StringBuilder fullResponse = new StringBuilder();
        responseFlux.subscribe(
            chunk -> fullResponse.append(chunk),
            error -> {},
            () -> {
                try {
                    if (StpUtil.isLogin()) {
                        saveMessage(conversationId, false, fullResponse.toString());
                    }
                } catch (Exception e) {
                    // 忽略保存错误
                }
            }
        );
        
        return responseFlux;
    }

    /**
     * SSE 流式调用 LoveApp AI 恋爱大师
     * @param message 用户输入的消息
     * @param conversationId 聊天会话 ID
     * @return 返回聊天结果
     */
    // 返回 ؜Flux 对象，并且‌设置泛型为 ServerSentEvent。使用这种方式可以‍省略 MediaType
    @GetMapping(value = "/love_app/chat/server_sent_event")
    public Flux<ServerSentEvent<String>> doChatWithLoveAppServerSentEvent(String message, String conversationId) {
        // 尝试保存用户消息
        try {
            if (StpUtil.isLogin()) {
                saveMessage(conversationId, true, message);
            }
        } catch (Exception e) {
            // 可能未登录，非致命错误，忽略
        }
        
        StringBuilder fullResponse = new StringBuilder();
        
        return loveApp.doChatByStream(message, conversationId)
                .map(chunk -> {
                    fullResponse.append(chunk);
                    return ServerSentEvent.<String>builder()
                            .data(chunk)
                            .build();
                })
                .doOnComplete(() -> {
                    try {
                        if (StpUtil.isLogin()) {
                            saveMessage(conversationId, false, fullResponse.toString());
                        }
                    } catch (Exception e) {
                        // 忽略保存错误
                    }
                });
    }

    /**
     * SSE 流式调用 LoveApp AI 恋爱大师
     * @param message 用户输入的消息
     * @param conversationId 聊天会话 ID
     * @return 返回聊天结果
     */
    // 使用 ؜SSEEmitter，‌通过 send 方法持续向 SseEmitter 发送消息（‍有点像 IO 操作）
    @GetMapping(value = "/love_app/chat/server_sse_emitter")
    public SseEmitter doChatWithLoveAppServerSseEmitter(String message, String conversationId) {
        SseEmitter sseEmitter = new SseEmitter(180000L); // 设置超时时间为 3 分钟
        
        // 尝试保存用户消息
        try {
            if (StpUtil.isLogin()) {
                saveMessage(conversationId, true, message);
            }
        } catch (Exception e) {
            // 可能未登录，非致命错误，忽略
        }
        
        // 获取 Flux 响应数据流
        Flux<String> eventFlux = loveApp.doChatByStream(message, conversationId);
        
        StringBuilder fullResponse = new StringBuilder();
        
        // 通过直接订阅推送给SseEmitter
        eventFlux.subscribe(
                data -> {
                    try {
                        fullResponse.append(data);
                        sseEmitter.send(data);
                    } catch (Exception e) {
                        sseEmitter.completeWithError(e);
                    }
                },
                sseEmitter::completeWithError,
                () -> {
                    try {
                        if (StpUtil.isLogin()) {
                            saveMessage(conversationId, false, fullResponse.toString());
                        }
                        sseEmitter.complete();
                    } catch (Exception e) {
                        sseEmitter.completeWithError(e);
                    }
                }
        );
        // 返回
        return sseEmitter;
    }

    /**
     * 流式调用 Manus 超级智能体
     * @param message 用户输入的消息
     * @param conversationId 会话ID，可选
     * @return 流式返回聊天结果
     */
    @GetMapping(value = "/manus/chat/sse")
    public SseEmitter doChatWithManusSse(String message, String conversationId) {
        // 创建 SseEmitter 实例
        SseEmitter sseEmitter = new SseEmitter(180000L); // 3分钟超时
        
        // 尝试保存用户消息
        try {
            if (StpUtil.isLogin() && conversationId != null) {
                saveMessage(conversationId, true, message);
            }
        } catch (Exception e) {
            // 可能未登录，非致命错误，忽略
        }
        
        // 创建MyManus实例
        MyManus myManus = new MyManus(availableTools, dashscopeChatModel);
        
        // 包装SseEmitter来捕获返回的消息
        SseEmitter wrappedEmitter = new SseEmitter(180000L) {
            StringBuilder fullResponse = new StringBuilder();
            
            @Override
            public void send(Object data) throws Exception {
                String strData = data.toString();
                fullResponse.append(strData);
                sseEmitter.send(strData);
            }
            
            @Override
            public void complete() {
                try {
                    if (StpUtil.isLogin() && conversationId != null) {
                        saveMessage(conversationId, false, fullResponse.toString());
                    }
                } catch (Exception e) {
                    // 忽略保存错误
                }
                sseEmitter.complete();
            }
            
            @Override
            public void completeWithError(Throwable ex) {
                sseEmitter.completeWithError(ex);
            }
        };
        
        // 执行Manus智能体
        myManus.runStreamWithEmitter(message, wrappedEmitter);
        
        return sseEmitter;
    }
    
    /**
     * 保存消息到会话
     */
    private void saveMessage(String conversationId, boolean isUser, String content) {
        // 只有登录状态才保存消息
        if (StpUtil.isLogin() && conversationId != null && !conversationId.isEmpty()) {
            try {
                conversationService.addMessage(conversationId, isUser, content);
            } catch (Exception e) {
                // 记录日志但不影响主流程
                e.printStackTrace();
            }
        }
    }
}
