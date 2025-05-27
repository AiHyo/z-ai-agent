package com.aih.zaiagent.agent;

import com.aih.zaiagent.advisor.MyLoggerAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;

/**
 * @author AiHyo
 */
@Component
public class MyManus extends TooCallAgent{


    public MyManus(ToolCallback[] availableTools, ChatModel dashscopeChatModel) {
        super(availableTools);
        this.setName("MyManus");
        this.setSystemPrompt("""
                You are AiHyoManus, an all-capable AI assistant, aimed at solving any task presented by the user.
                You have various tools at your disposal that you can call upon to efficiently complete complex requests.
                """);
        this.setNextStepPrompt("""
                Based on user needs, proactively select the most appropriate tool or combination of tools.
                For complex tasks, you can break down the problem and use different tools step by step to solve it.
                After using each tool, clearly explain the execution results and suggest the next steps.
                If you want to stop the interaction at any point, use the `terminateTool` tool/function call.
                """);
        this.setMaxSteps(20);
        // 初始化 AI 对话客户端
        ChatClient chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultAdvisors(new MyLoggerAdvisor())
                .build();
        this.setChatClient(chatClient);
    }
}
