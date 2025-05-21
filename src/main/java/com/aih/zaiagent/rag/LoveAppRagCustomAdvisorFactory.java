package com.aih.zaiagent.rag;

import org.springframework.ai.chat.client.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;

/**
 * 工厂类： RAG 检索增强顾问
 * @author AiHyo
 */
public class LoveAppRagCustomAdvisorFactory {

    /**
     * 创建自定义的 RAG 检索增强顾问
     * 指定向量存储和状态标签
     * @param vectorStore 向量存储
     * @param status      状态
     * @return Advisor 自定义的 RAG 检索增强顾问
     */
    public static Advisor createLoveAppRagCustomAdvisor(VectorStore vectorStore, String status) {
        // 通过spring ai内置的 filterExpression 配置过滤规则
        Filter.Expression expression = new FilterExpressionBuilder()
                .eq("status", status)
                .build();
        // 创建 向量文档检索器
        DocumentRetriever documentRetriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)   //向量存储
                .filterExpression(expression) // 过滤规则
                .similarityThreshold(0.5)   // 相似度阈值
                .topK(3) // 返回文档数量
                .build();
        // 创建自定义的 检索增强顾问Advisor 并返回
        return RetrievalAugmentationAdvisor.builder()
                // 使用自定义的文档检索器
                .documentRetriever(documentRetriever)
                // 使用自定义的上下文查询增强器
                .queryAugmenter(LoveAppContextualQueryAugmenterFactory.createInstance())
                .build();
    }
}
