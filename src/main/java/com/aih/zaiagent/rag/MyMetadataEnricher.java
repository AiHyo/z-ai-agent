package com.aih.zaiagent.rag;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.KeywordMetadataEnricher;
import org.springframework.ai.transformer.SummaryMetadataEnricher;
import org.springframework.stereotype.Component;

import java.util.List;

// MetadataEnricher 元数据增强器 预处理文档，ETL中的transform
// 基于 AI 的文档元数据增强器（为文本补充元数据）
@Component
class MyMetadataEnricher {

    // 自动注入 dashscopeChatModel
    @Resource
    private ChatModel dashscopeChatModel;

    // 关键词元数据增强器
    // KeywordMetadataEnricher：使用 AI 提取关键词并添加到元数据，metadata中会多出excerpt_keywords
    List<Document> enrichDocumentsByKeyword(List<Document> documents) {
        KeywordMetadataEnricher enricher = new KeywordMetadataEnricher(this.dashscopeChatModel, 5);
        return enricher.apply(documents);
    }

    // 摘要元数据增强器
    // SummaryMetadataEnricher：使用 AI 生成文档摘要并添加到元数据。
    // 不仅可以为当前文档生成摘要，还能关联前一个和后一个相邻的文档，让摘要更完整。
    List<Document> enrichDocumentsBySummary(List<Document> documents) {
        SummaryMetadataEnricher enricher = new SummaryMetadataEnricher(dashscopeChatModel,
            List.of(SummaryMetadataEnricher.SummaryType.PREVIOUS, SummaryMetadataEnricher.SummaryType.CURRENT, SummaryMetadataEnricher.SummaryType.NEXT));
        return enricher.apply(documents);
    }
}
