package com.aih.zaiagent.app;

import com.aih.zaiagent.advisor.MyLoggerAdvisor;
import com.aih.zaiagent.advisor.ReReadingAdvisor;
import com.aih.zaiagent.chatmemory.FileBaseChatMemory;
import com.aih.zaiagent.rag.LoveAppRagCustomAdvisorFactory;
import com.aih.zaiagent.rag.QueryRewriter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

/**
 * @author AiHyo
 */
@Slf4j
@Component
public class LoveApp {

    private final ChatClient chatClient;

    private static final String SYSTEM_PROMPT = "扮演深耕恋爱心理领域的专家。" +
            "开场向用户表明身份，告知用户可倾诉恋爱难题。" +
            "围绕单身、恋爱、已婚三种状态提问：" +
            "单身状态询问社交圈拓展及追求心仪对象的困扰;" +
            "恋爱状态询问沟通、习惯差异引发的矛盾;" +
            "已婚状态询问家庭责任与亲属关系处理的问题。" +
            "引导用户详述事情经过、对方反应及自身想法，以便给出专属解决方案。";

    /**
     * 构造函数：使用 dashScopeChatModel 创建 ChatClient
     */
    public LoveApp(ChatModel dashscopeChatModel) {
        // 初始化基于【文件】的对话记忆
        String fileDir = System.getProperty("user.dir") + "/chat-memory";
        ChatMemory chatMemory = new FileBaseChatMemory(fileDir);
        // 初始化基于【内存】的对话记忆
        // ChatMemory chatMemory = new InMemoryChatMemory();
        this.chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(chatMemory),
                        // 自定义的日志拦截器，按需开启
                        new MyLoggerAdvisor(),
                        // re2的日志拦截器，按需开启
                        new ReReadingAdvisor()
                )
                .build();
    }

    /**
     * AI 基础对话（支持多轮对话记忆）
     * @param message
     * @param chatId
     */
    public String doChat(String message, String chatId){
        ChatResponse chatResponse = chatClient.prompt()
                .user(message)
                .advisors(advisorSpec -> advisorSpec
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId) // 设置对话记忆的会话ID
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10)    // 设置对话记忆的检索大小
                )
                .call()
                .chatResponse();
        String content = chatResponse != null ? chatResponse.getResult().getOutput().getText() : null;
        // log.info("content: {}", content);
        return content;
    }

    record LoveReport(String title, List<String> suggestions){

    }

    /**
     * AI 恋爱报告功能（实战结构化输出）
     * @param message
     * @param chatId
     */
    public LoveReport doChatWithReport(String message, String chatId){
        LoveReport loveReport = chatClient
                .prompt()
                .system(SYSTEM_PROMPT + "每次对话后都要给出一份恋爱报告，标题为{用户名}的恋爱报告，内容建议为列表")
                .user(message)
                .advisors(advisorSpec -> advisorSpec
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId) // 设置对话记忆的会话ID
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10)    // 设置对话记忆的检索大小
                )
                .call()
                .entity(LoveReport.class);
        log.info("content: {}", loveReport);
        return loveReport;
    }


    @Resource
    private VectorStore loveAppVectorStore;
    @Resource
    private VectorStore pgVectorVectorStore;
    @Resource
    private QueryRewriter queryRewriter;
    // 应用RAG检索增强服务（基于内存 / PgVector的向量存储）
    public String doChatWithRag(String message, String chatId) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                // .user(queryRewriter.doQueryRewrite(message)) // 应用基于AI的查询重写器
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10)) // 设置对话记忆的会话ID 和 记忆大小
                // 开启日志，便于观察效果
                .advisors(new MyLoggerAdvisor())
                // 应用RAG检索增强顾问（基于内存的本地向量存储）
                // .advisors(new QuestionAnswerAdvisor(loveAppVectorStore))
                // 应用RAG检索增强顾问（基于PgVector的向量存储）
                .advisors(new QuestionAnswerAdvisor(pgVectorVectorStore))
                // 应用自定义的RAG检索增强顾问，在向量存储基础上，添加其他条件（文档查询器【自定义：向量存储+检索条件】 + 上下文增强器【自定义：上下文为空时候，根据指定模板回答】）
                // .advisors(LoveAppRagCustomAdvisorFactory.createLoveAppRagCustomAdvisor(
                //         loveAppVectorStore, "已婚"
                // ))
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }


    @Resource
    private Advisor loveAppRagCloudAdvisor;
    // 应用云知识库问答
    public String doChatWithRagCloud(String message, String chatId) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                // 开启日志，便于观察效果
                .advisors(new MyLoggerAdvisor())
                // 应用RAG检索增强服务（基于云知识库的向量存储）
                .advisors(loveAppRagCloudAdvisor)
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    @Resource
    private ToolCallback[] allTools;
    // 提供 AI 工具调用
    public String doChatWithTools(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                // 开启日志，便于观察效果
                .advisors(new MyLoggerAdvisor())
                // 添加可用的工具
                .tools(allTools)
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    @Resource
    private ToolCallbackProvider toolCallbackProvider;
    // AI 调用 MCP 服务
    public String doChatWithMcp(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                // 开启日志，便于观察效果
                .advisors(new MyLoggerAdvisor())
                // 通过自动注入的 ToolCallbackProvider 获取到配置中定义的 MCP 服务提供的 所有工具，并提供给 ChatClient
                .tools(toolCallbackProvider)
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }




}
