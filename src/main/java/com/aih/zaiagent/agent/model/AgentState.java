package com.aih.zaiagent.agent.model;

/**
 * 代理执行状态的枚举类
 * @author AiHyo
 */
public enum AgentState {
    IDLE,     // 空闲，初始状态
    RUNNING,  // 运行中
    FINISHED, // 已完成
    ERROR     // 错误
}
