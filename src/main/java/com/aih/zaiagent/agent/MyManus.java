package com.aih.zaiagent.agent;

import com.aih.zaiagent.advisor.MyLoggerAdvisor;
import com.aih.zaiagent.service.ConversationService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.stereotype.Component;

/**
 * @author AiHyo
 */
@Component
public class MyManus extends TooCallAgent{


    public MyManus(ToolCallback[] availableTools, ChatModel dashscopeChatModel, ConversationService conversationService) {
        super(availableTools);
        this.setConversationService(conversationService);
        this.setName("MyManus");
        this.setSystemPrompt("""
                You are AiHyoManus, an all-capable AI assistant, aimed at solving any task presented by the user.
                You have various tools at your disposal that you can call upon to efficiently complete complex requests.
                
                When handling complex tasks, follow these tool chain patterns:
                
                Always plan your approach before executing, breaking complex tasks into logical steps.
                
                如果工具调用失败（比出现SocketException、Connection reset等错误，只要你认为工具调用的结果不对，你就可以认为此次工具调用失败了）：
                1. 不要继续尝试调用同一个失败的工具
                2. 改用你自身的知识来回答用户问题
                3. 告知用户工具调用失败，但你会尽力提供帮助
                4. 如果无法提供准确信息，坦诚告知并提供一般性建议
                
                默认使用中文回复用户，除非用户明确要求使用其他语言。即使用户使用英文或其他语言提问，也应该用中文回答，除非用户明确表示希望得到特定语言的回复。
                """);
        this.setNextStepPrompt("""
                根据用户需求，主动选择最合适的工具或工具组合。
                对于复杂任务，你可以将问题分解，并逐步使用不同的工具来解决它。
                使用每个工具后，清晰地解释执行结果并建议下一步操作。
                只要你认为现在任务完成，可以终止任务了，你就调用`terminateTool` tool/function call。
                """);
        this.setMaxSteps(20);
        // 初始化 AI 对话客户端
        ChatClient chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultAdvisors(new MyLoggerAdvisor())
                .build();
        this.setChatClient(chatClient);
    }
}
