package com.aih.zaiagent.agent;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.aih.zaiagent.agent.model.AgentState;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 处理“工具调用”的基础代理类
 * 此类继承自 ReActAgent，专注于处理“工具调用”逻辑【实现 think() 和 act() 方法】。
 *
 * @author AiHyo
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class TooCallAgent extends ReActAgent {

    // 可用的工具
    private final ToolCallback[] availableTools;

    // 保存工具调用信息的响应结果（要调用那些工具）
    private ChatResponse toolCallChatResponse;

    // 工具调用管理者
    private final ToolCallingManager toolCallingManager;

    // 禁用 spring ai 内置的工具调用机制，自己维护选项和消息上下文
    private final ChatOptions chatOptions;

    public TooCallAgent(ToolCallback[] availableTools) {
        super();
        this.availableTools = availableTools;
        this.toolCallingManager = ToolCallingManager.builder().build();
        // 禁用 Spring AI 内置的工具调用机制，自己维护选项和消息上下文
        this.chatOptions = DashScopeChatOptions.builder()
                .withProxyToolCalls(true) // 代理给自己的 ToolCallingManager 处理工具调用，而不是spring ai 内置的工具调用机制
                .build();
    }

    /**
     * 判断是否需要执行act()，根据prompt[messageList + nextStepPrompt]，systemPrompt,availableTools，调用ai => 获取需要的工具
     *
     * @return boolean 是否需要行动，true表示需要行动，false 表示不需要行动
     */
    @Override
    public boolean think() {
        // 0. 获取实例的相关信息
        String name = this.getName();
        String nextStepPrompt = this.getNextStepPrompt();
        List<Message> messageList = this.getMessageList();
        // 1. 提示词，拼接用户提示词
        if (StrUtil.isNotBlank(nextStepPrompt)) {
            messageList.add(new UserMessage(nextStepPrompt));
        }
        try {
            // 2. 调用 AI 大模型，返回需要调用的工具列表，[传入自定义的chatOptions配置，禁用 spring ai 内置的工具调用机制]
            Prompt prompt = new Prompt(messageList, chatOptions);
            ChatResponse chatResponse = this.getChatClient().prompt(prompt)
                    .system(this.getSystemPrompt())
                    .tools(availableTools)
                    // .advisors(advisorSpec -> advisorSpec
                    //         .param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId) // 设置对话记忆的会话ID
                    //         .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 20)    // 设置对话记忆的检索大小
                    // )
                    // .advisors(new MessageChatMemoryAdvisor(new DatabaseChatMemory(this.getConversationService(), userId)))
                    .call()
                    .chatResponse();
            // 记录 ai 响应 => 需要调用的工具列表[在act() 方法中需要使用]，若不为空，再act()
            this.toolCallChatResponse = chatResponse;
            // 3. 解析工具调用结果，获取要调用的工具
            // 从chatResponse中获取助手消息assistantMessage
            AssistantMessage assistantMessage = chatResponse.getResult().getOutput();
            // 从assistantMessage中获取 ①输出提示信息result ②工具调用列表toolCallList
            String result = assistantMessage.getText();
            this.setThinkResult(result); // 记录AI思考结果，用于返回
            log.info("AI 思考过程：{}", getThinkResult());
            // this.getConversationService().saveMessage(this.getChatId(), this.getUserId(), result, "ai"); // 返回到baseAgent再处理
            List<AssistantMessage.ToolCall> toolCallList = assistantMessage.getToolCalls();
            String toolCallInfo = toolCallList.stream()
                    .map(toolCall -> String.format("工具：%s, 参数：%s", toolCall.name(), toolCall.arguments()))
                    .collect(Collectors.joining("\n"));
            log.info(toolCallInfo);
            // 如果不需要调用工具，返回 false
            if (toolCallList.isEmpty()) {
                // 不调用工具时，需要手动记录助手消息
                messageList.add(assistantMessage);
                // 多重保险：当没有工具需要调用时，设置状态为FINISHED
                this.setState(AgentState.FINISHED);
                return false;
            } else {
                // 直接返回，不用手动记录助手消息：在act时会记录
                return true;
            }
        } catch (Exception e) {
            log.error("{}的思考过程遇到了问题：{}", name, e.getMessage());
            messageList.add(new AssistantMessage("处理时遇到了错误：" + e.getMessage()));
            // 多重保险：发生异常时，设置状态为ERROR
            this.setState(AgentState.ERROR);
            return false;
        }
    }

    /**
     * 执行工具调用并处理结果【更新消息列表，返回此次工具调用的结果】
     *
     * @return 执行结果
     */
    @Override
    public String act() {
        if (!toolCallChatResponse.hasToolCalls()) {
            return "没有工具需要调用";
        }
        // 1. act。构造prompt，手动调用工具，获取工具调用结果toolExecutionResult
        Prompt prompt = new Prompt(this.getMessageList(), chatOptions);
        ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, toolCallChatResponse);
        // 2. 更新消息列表
        // 更新消息列表：toolExecutionResultMessage：会将最新的助手消息和工具调用返回的结果，拼接到之前的消息列表，所以能直接set
        List<Message> toolExecutionResultMessage = toolExecutionResult.conversationHistory();
        this.setMessageList(toolExecutionResultMessage);
        // 3. 获取工具调用的返回消息：即toolExecutionResultMessage的最后一条
        ToolResponseMessage toolResponseMessage = (ToolResponseMessage) CollUtil.getLast(toolExecutionResultMessage);
        // 特判，如果调用的是终止工具，更新状态为 FINISHED
        if (toolResponseMessage.getResponses().stream().anyMatch(response -> "doTerminate".equals(response.name()))) {
            this.setState(AgentState.FINISHED);
        }
        // 3. 记录工具调用结果results，并返回
        String results = toolResponseMessage.getResponses().stream()
                .map(response -> "工具 " + response.name() + " 返回的结果：" + response.responseData())
                .collect(Collectors.joining("\n"));
        log.info(results);
        // this.getConversationService().saveMessage(this.getChatId(), this.getUserId(), results, "ai"); // 返回到baseAgent再处理
        return results;
    }
}
