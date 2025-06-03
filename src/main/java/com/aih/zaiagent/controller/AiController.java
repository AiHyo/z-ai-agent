package com.aih.zaiagent.controller;

import com.aih.zaiagent.agent.MyManus;
import com.aih.zaiagent.app.LoveApp;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
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
@RequestMapping("/ai")
public class AiController {

    @Resource
    private LoveApp loveApp;

    @Resource
    private ToolCallback[] availableTools;

    @Resource
    private DashScopeChatModel dashscopeChatModel;


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
     * @param chatId 聊天会话 ID
     * @return 返回聊天结果
     */
    // 返回؜ Flux 响应式‌对象，并且添加  SSE 对应的 MediaType
    @GetMapping(value = "/love_app/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatWithLoveAppSse(String message, String chatId) {
        return loveApp.doChatByStream(message, chatId);
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
    public SseEmitter doChatWithManusSse(String message) {
        MyManus myManus = new MyManus(availableTools, dashscopeChatModel);
        return myManus.runStream(message);
    }



}
