package com.aih.zaiagent.rag;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
class PgVectorVectorStoreConfigTest {

    // 注入 手动整合的 PgVectorVectorStore
    @Resource
    private VectorStore pgVectorVectorStore;

    @Test
    void pgVectorVectorStore() {
        List<Document> documents = List.of(
                new Document("pgvector支持近似最近邻（ANN）搜索" +
                        "这对于大规模数据的高效检索很重要，比精确搜索更快", Map.of("meta1", "meta1")),
                new Document("pgvector的好处可能包括高效的向量相似性搜索，这在RAG的检索阶段很重要。" +
                        "pgvector和PostgreSQL集成，这对Java项目来说可能更容易，" +
                        "因为PostgreSQL是常用的关系型数据库"),
                new Document("AiHyo这小伙子比较帅气", Map.of("meta2", "meta2")));
        // 添加文档
        pgVectorVectorStore.add(documents);
        // 相似度查询
        SearchRequest searchRequest = SearchRequest.builder()
                .query("pgvector的好处是啥")
                .topK(3)
                .build();
        List<Document> results = pgVectorVectorStore.similaritySearch(searchRequest);
        assertNotNull(results);
        log.info("results: {}", results);
    }
}
