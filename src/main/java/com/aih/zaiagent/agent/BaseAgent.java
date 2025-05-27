package com.aih.zaiagent.agent;

import cn.hutool.core.util.StrUtil;
import com.aih.zaiagent.agent.model.AgentState;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * 抽象基础代理类
 * 子类需要实现具体的步骤step逻辑
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

    // Memory 记忆 (需要自主维护会话上下文)
    private List<Message> messageList = new ArrayList<>();

    /**
     * 运行代理
     * @param userPrompt 用户输入的提示词
     * @return 执行结果
     */
    public String run(String userPrompt){
        // 1. 校验
        if(this.state != AgentState.IDLE){
            log.warn("Agent is not idle, current state: {}", this.state);
            return "Agent is busy, please try again later.";
        }
        if(StrUtil.isBlank(userPrompt)){
            log.warn("User prompt is empty");
            return "User prompt cannot be empty.";
        }
        // 2. 执行
        // 设置状态为运行中
        this.state = AgentState.RUNNING;
        // 记录消息上下文
        this.messageList.add(new UserMessage(userPrompt));
        // 结果列表
        List<String> results = new ArrayList<>();

        try {
            // 循环执行，直到达到最大步骤数或状态变为 FINISHED
            while(state != AgentState.FINISHED) {
                if (currentStep >= maxSteps) {
                    log.warn("Reached maximum steps: {}", maxSteps);
                    this.state = AgentState.FINISHED;
                    // 记录结果，结束
                    results.add("Reached maximum steps, please try again with a different prompt.");
                    break;
                }
                // 更新当前步骤
                this.currentStep++;
                // 执行单个步骤
                String stepResult = step();
                String result = "Step " + currentStep + ": " + stepResult;
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
            // 3. 清理资源
            this.cleanup();
        }
    }

    /**
     * 单个步骤
     */
    public abstract String step();

    /**
     * 清理资源
     */
    protected void cleanup(){
        // 子类可以重写此方法来清理资源
    }



}
