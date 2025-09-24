package com.aih.zaiagent.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.aih.zaiagent.agent.MyManus;
import com.aih.zaiagent.app.LoveApp;
import com.aih.zaiagent.service.ConversationService;
import com.aih.zaiagent.service.UserService;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import jakarta.annotation.Resource;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author AiHyo
 */
@RestController
@RequestMapping("/ai")
public class AiController {

    private static final Logger log = LoggerFactory.getLogger(AiController.class);

    @Value("${love-app.chat-memory-size}")
    private int loveAppChatMemorySize;

    @Value("${manus.chat-memory-size}")
    private int manusChatMemorySize;

    @Resource
    private LoveApp loveApp;

    @Resource
    private ToolCallback[] availableTools;

    @Resource
    private ToolCallbackProvider toolCallbackProvider;

    @Resource
    private DashScopeChatModel dashscopeChatModel;

    @Resource
    private ConversationService conversationService;

    @Resource
    private UserService userService;

    // 用于临时存储流式响应的完整内容
    private final ConcurrentHashMap<String, StringBuilder> responseBuilders = new ConcurrentHashMap<>();

    /**
     * 同步调用 LoveApp AI 恋爱大师
     * @param message 用户输入的消息
     * @param chatId 聊天会话 ID
     * @return 返回聊天结果
     */
    @GetMapping("/love_app/chat/sync")
    public String doChatWithLoveAppSync(String message, String chatId) {
        return loveApp.doChat(message, chatId);
    }

    /**
     * SSE 流式调用 LoveApp AI 恋爱大师
     * @param message 用户输入的消息
     * @param conversationId 会话ID (用于聊天记忆和保存消息)
     * @return 返回聊天结果
     */
    // 返回؜ Flux 响应式‌对象，并且添加  SSE 对应的 MediaType
    @GetMapping(value = "/love_app/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatWithLoveAppSse(String message,
                                             @RequestParam String conversationId,
                                             HttpServletRequest request) {
        StpUtil.checkLogin(); // 校验登陆

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                log.info("Cookie: {} = {}", cookie.getName(), cookie.getValue());
            }
        }
        // 获取原始响应流
        log.info("begin dochat: {}", conversationId);
        long userId = StpUtil.getLoginIdAsLong();
        Flux<String> originalFlux = loveApp.doChatByStream(message, conversationId, userId);
        log.info("end dochat: {}", conversationId);
        return originalFlux;
        // try {
        //     // 获取当前用户id
        //     Long userId = StpUtil.getLoginIdAsLong();
        //     // 保存用户消息
        //     conversationService.saveMessage(conversationId, userId, message, "user");
        //
        //     // 初始化响应构建器
        //     String responseKey = conversationId + "_" + LocalDateTime.now().toString();
        //     responseBuilders.put(responseKey, new StringBuilder());
        //
        //     // 收集完整响应并在结束时保存
        //     final Long finalUserId = userId;  // 创建一个final变量用于lambda表达式
        //     return originalFlux
        //             .doOnNext(chunk -> {
        //                 // 累积响应内容
        //                 responseBuilders.get(responseKey).append(chunk);
        //             })
        //             .doOnComplete(() -> {
        //                 // 在流结束时获取完整响应并保存
        //                 String fullResponse = responseBuilders.get(responseKey).toString();
        //
        //                 // 保存AI回复
        //                 conversationService.saveMessage(
        //                         conversationId,
        //                         finalUserId,
        //                         fullResponse,
        //                         "ai"
        //                 );
        //
        //                 // 更新会话最后消息
        //                 conversationService.updateLastMessage(conversationId, fullResponse);
        //
        //                 // 清理临时存储
        //                 responseBuilders.remove(responseKey);
        //             })
        //             .doOnError(e -> responseBuilders.remove(responseKey));
        // } catch (Exception e) {
        //     // 出现任何异常，返回原始流，不保存消息
        //     return originalFlux;
        // }
    }

    /**
     * SSE 流式调用 LoveApp AI 恋爱大师
     * @param message 用户输入的消息
     * @param chatId 聊天会话 ID
     * @return 返回聊天结果
     */
    // 返回 ؜Flux 对象，并且‌设置泛型为 ServerSentEvent。使用这种方式可以‍省略 MediaType
    @GetMapping(value = "/love_app/chat/server_sent_event")
    public Flux<ServerSentEvent<String>> doChatWithLoveAppServerSentEvent(String message, String chatId) {
        return loveApp.doChatByStream(message, chatId)
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build());
    }

    /**
     * SSE 流式调用 LoveApp AI 恋爱大师
     * @param message 用户输入的消息
     * @param chatId 聊天会话 ID
     * @return 返回聊天结果
     */
    // 使用 ؜SSEEmitter，‌通过 send 方法持续向 SseEmitter 发送消息（‍有点像 IO 操作）
    @GetMapping(value = "/love_app/chat/server_sse_emitter")
    public SseEmitter doChatWithLoveAppServerSseEmitter(String message, String chatId) {
        SseEmitter sseEmitter = new SseEmitter(180000L); // 设置超时时间为 3 分钟
        // 获取 Flux 响应数据流
        Flux<String> eventFlux = loveApp.doChatByStream(message, chatId);
        // 通过直接订阅推送给SseEmitter
        eventFlux.subscribe(
                data -> {
                    try {
                        sseEmitter.send(data);
                    } catch (Exception e) {
                        sseEmitter.completeWithError(e);
                    }
                },
                sseEmitter::completeWithError,
                sseEmitter::complete
        );
        // 返回
        return sseEmitter;
    }

    /**
     * 流式调用 Manus 超级智能体
     * @param message 用户输入的消息
     * @return 流式返回聊天结果
     */
    @GetMapping(value = "/manus/chat/sse")
    public SseEmitter doChatWithManusSse(String message, String conversationId) {
        StpUtil.checkLogin(); // 校验登陆
        long userId = StpUtil.getLoginIdAsLong();
        MyManus myManus = new MyManus(availableTools, dashscopeChatModel, conversationService);
        // 初始化会话消息列表
        myManus.initMessageList(conversationId, manusChatMemorySize, userId);
        return myManus.runStream(message);
    }
}
