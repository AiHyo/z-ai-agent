package com.aih.zaiagent.rag;

import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 初始化基于内存的向量存储 Bean
 * @author AiHyo
 */
@Configuration
public class LoveAppVectorStoreConfig {

    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;
    @Resource
    private MyTokenTextSplitter myTokenTextSplitter;
    @Resource
    private MyMetadataEnricher myMetadataEnricher;

    @Bean
    VectorStore loveAppVectorStore(EmbeddingModel dashscopeEmbeddingModel) {
        // 创建向量存储
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(dashscopeEmbeddingModel)
                .build();
        // 加载文档
        List<Document> documents = loveAppDocumentLoader.loadMarkdowns();
        // 自主切分
        // List<Document> splitDocuments = myTokenTextSplitter.splitDocuments(documents);
        // 增强元数据, 基于AI自动补充关键词元信息：metadata中会多出excerpt_keywords
        List<Document> enrichedDocuments = myMetadataEnricher.enrichDocumentsByKeyword(documents);
        simpleVectorStore.add(enrichedDocuments);
        return simpleVectorStore;
    }
}
