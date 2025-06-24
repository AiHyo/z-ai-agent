package com.aih.zaiagent.agent;

import com.aih.zaiagent.agent.model.AgentState;
import com.aih.zaiagent.service.ConversationService;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;

/**
 * ReAct (Reasoning and Acting) 模式的代理抽象类
 * 实现了思考-行动的循环模式
 * 子类需要实现具体的思考think和行动act逻辑
 * @author AiHyo
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public abstract class ReActAgent extends BaseAgent{

    /**
     * 处理当前状态并决定下一步行动
     * @return 是否需要执行行动，true表示需要执行，false表示不需要执行
     */
    public abstract boolean think();

    /**
     * 执行决定的行动
     *
     * @return 行动执行结果
     */
    public abstract String act();

    /**
     * 执行单个步骤：思考和行动
     * @return 步骤执行结果
     */
    @Override
    public String[] step() {
        try {
            // 根据思考结果决定是否需要行动
            if (think()) {
                String actResult = act();
                return new String[]{getThinkResult(), actResult};
            }
            // 思考完成无需行动时，设置状态为FINISHED
            this.setState(AgentState.FINISHED);
            return new String[]{getThinkResult(), "思考完成 - 无需行动"};
        } catch (Exception e) {
            log.error("执行步骤时发生错误: {}", e.getMessage(), e);
            // 发生异常时，设置状态为ERROR或FINISHED
            this.setState(AgentState.ERROR);
            return new String[]{"执行步骤时发生错误: " + e.getMessage()};
        }
    }

}
