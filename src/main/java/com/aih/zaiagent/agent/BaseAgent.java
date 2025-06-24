package com.aih.zaiagent.agent;

import cn.hutool.core.util.StrUtil;
import com.aih.zaiagent.agent.model.AgentState;
import com.aih.zaiagent.chatmemory.DatabaseChatMemory;
import com.aih.zaiagent.service.ConversationService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 抽象基础代理类
 * 子类需要实现具体的步骤step逻辑
 *
 * @author AiHyo
 */
@Data
@Slf4j
public abstract class BaseAgent {

    // 代理名称
    private String name;

    // 提示词
    private String systemPrompt;
    private String nextStepPrompt;

    // 代理状态
    private AgentState state = AgentState.IDLE; // 初始状态为 IDLE

    // 执行步骤控制
    private int currentStep = 0;
    private int maxSteps = 20; // 最大步骤数，默认 20

    // LLM 大语言模型
    private ChatClient chatClient;

    // ChatMemory 需要
    private String chatId;
    private Long userId;
    private ConversationService conversationService;

    // Memory 记忆 (需要自主维护会话上下文)
    private List<Message> messageList;

    // 存储当前思考结果
    private String thinkResult;

    /**
     * 运行代理
     *
     * @param userPrompt 用户输入的提示词
     * @return 执行结果
     */
    public String run(String userPrompt) {
        // 1. 校验
        if (this.state != AgentState.IDLE) {
            log.warn("Agent is not idle, current state: {}", this.state);
            return "Agent is busy, please try again later.";
        }
        if (StrUtil.isBlank(userPrompt)) {
            log.warn("User prompt is empty");
            return "User prompt cannot be empty.";
        }
        // 2. 准备执行
        // 设置状态，并记录消息，创建结果列表
        this.state = AgentState.RUNNING;
        this.messageList.add(new UserMessage(userPrompt));
        List<String> results = new ArrayList<>();
        // 3. 开始执行
        try {
            // 循环执行，直到达到最大步骤数或状态变为 FINISHED
            int i = 0;
            while (state != AgentState.FINISHED) {
                i += 1;
                currentStep = i;
                // 若果超过最大步骤数，则结束执行
                if (currentStep > maxSteps) {
                    log.warn("Reached maximum steps: {}", maxSteps);
                    this.state = AgentState.FINISHED;
                    results.add("Reached maximum steps, please try again with a different prompt.");
                    break;
                }
                // 执行单个步骤
                String[] stepResult = step();
                // 拼接结果
                String stepResultStr = String.join("\n", stepResult);
                String result = "Step " + currentStep + ": " + stepResultStr;
                // 记录结果
                results.add(result);
            }
            // 结束后，结果拼接成字符串并返回
            return String.join("\n", results);
        } catch (Exception e) {
            this.state = AgentState.ERROR;
            log.error("Error occurred during agent execution: {}", e.getMessage(), e);
            return "An error occurred during execution: " + e.getMessage();
        } finally {
            // 4. 清理资源
            this.cleanup();
        }
    }

    /**
     * 运行代理(流式输出)
     *
     * @param userPrompt 用户输入的提示词
     * @return 执行结果
     */
    public SseEmitter runStream(String userPrompt) {
        // 流式输出的 run 方法，需要额外添加 SseEmitter 支持
        SseEmitter sseEmitter = new SseEmitter(300000L); // 设置超时时间为 5 分钟

        // 使用线程异步处理，避免阻塞主线程
        CompletableFuture.runAsync(() -> {
            try {
                // 1. 校验
                if (this.state != AgentState.IDLE) {
                    sseEmitter.send("Agent is busy, please try again later.");
                    conversationService.saveMessage(chatId, userId, "Agent is busy, please try again later.", "ai");
                    sseEmitter.complete(); // 手动标记事件流完成，关闭连接
                    return;
                }
                if (StrUtil.isBlank(userPrompt)) {
                    sseEmitter.send("User prompt cannot be empty.");
                    conversationService.saveMessage(chatId, userId, "User prompt cannot be empty.", "ai");
                    sseEmitter.complete();
                    return;
                }
            } catch (Exception e){
                sseEmitter.completeWithError(e);
            }
            // 2. 准备执行
            // 设置状态，并记录消息，创建结果列表
            this.state = AgentState.RUNNING;
            conversationService.saveMessage(chatId, userId, userPrompt, "user");
            this.messageList.add(new UserMessage(userPrompt));
            // 3. 开始执行
            try {
                int i = 0;
                // 循环执行，直到达到最大步骤数或状态变为 FINISHED
                while (state != AgentState.FINISHED) {
                    i += 1;
                    currentStep = i;
                    // 如果超过最大步骤数，则结束执行
                    if (currentStep > maxSteps) {
                        log.warn("Reached maximum steps: {}", maxSteps);
                        this.state = AgentState.FINISHED;
                        sseEmitter.send("Reached maximum steps, please try again with a different prompt.");
                        conversationService.saveMessage(chatId, userId, "Reached maximum steps, please try again with a different prompt.", "ai");
                        break;
                    }
                    // 执行单个步骤，发送结果到客户端[thinkResult + actResult]
                    String[] stepResult = step();
                    sseEmitter.send("Step " + currentStep + ": " );
                    conversationService.saveMessage(chatId, userId, "Step " + currentStep + ": ", "ai");
                    for (String result : stepResult) {
                        log.info("sseEmitter.send,{}",result);
                        sseEmitter.send(result.replace("\n", "<br>") + "<br>");
                        conversationService.saveMessage(chatId, userId, result, "ai");
                    }
                }
                // 结束后，发送完成消息
                try {
                    // 发送一个特殊的结束标记
                    sseEmitter.send(SseEmitter.event().name("complete").data("[DONE]"));
                    // 然后完成连接
                    sseEmitter.complete();
                } catch (Exception e) {
                    log.error("发送完成消息时出错", e);
                    sseEmitter.completeWithError(e);
                }
            } catch (Exception e) {
                this.state = AgentState.ERROR;
                log.error("Error occurred during agent execution: {}", e.getMessage(), e);
                try {
                    sseEmitter.send("An error occurred during execution: " + e.getMessage());
                    conversationService.saveMessage(chatId, userId, "An error occurred during execution: " + e.getMessage(), "ai");
                    sseEmitter.complete();
                } catch (Exception ex) {
                    sseEmitter.completeWithError(ex);
                }
            } finally {
                // 4. 合并消息并清理资源
                conversationService.mergeAiMessageByConversationId(chatId);
                this.cleanup();
            }
        });

        // 设置sseEmitter的超时和完成回调
        sseEmitter.onTimeout(() -> {
            conversationService.saveMessage(chatId, userId, "SSE emitter timed out", "ai");
            this.state = AgentState.ERROR; // 超时视为错误状态
            this.cleanup();
            log.warn("SSE emitter timed out");
            // sseEmitter.complete();
        });

        sseEmitter.onCompletion(()->{
            if(this.state == AgentState.RUNNING) {
                this.state = AgentState.FINISHED; // 如果在完成时仍然是运行状态，则设置为已完成
                log.info("SSE emitter completed successfully");
            }
            this.cleanup();
            log.info("SSE has completed and resources cleaned up");
        });

        // 添加错误回调
        sseEmitter.onError((ex) -> {
            this.state = AgentState.ERROR;
            this.cleanup();
            log.error("SSE emitter encountered an error: {}", ex.getMessage(), ex);
        });
        return sseEmitter;
    }

    /**
     * 单个步骤
     */
    public abstract String[] step();

    /**
     * 清理资源
     */
    protected void cleanup() {
        // 子类可以重写此方法来清理资源
    }

    // 初始化消息列表
    public void initMessageList(String conversationId, int lastN, Long userId) {
        this.chatId = conversationId;
        this.userId = userId;
        DatabaseChatMemory chatMemory = new DatabaseChatMemory(this.conversationService, userId);
        this.messageList = chatMemory.get(conversationId, lastN);
    }

}
