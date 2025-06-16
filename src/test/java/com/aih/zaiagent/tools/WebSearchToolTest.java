package com.aih.zaiagent.tools;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class WebSearchToolTest {

    @Value("${search-api.api-key}")
    private String apiKey;

    @Test
    void searchWebWithDefaultParams() {
        WebSearchTool webSearchTool = new WebSearchTool(apiKey);
        String query = "spring ai的工具调用";
        String res = webSearchTool.searchWeb(query);
        System.out.println("=== 默认参数测试 (5个结果, 3个问题) ===");
        System.out.println(res);
        assertNotNull(res);
        assertTrue(res.contains("搜索查询"));
        assertTrue(res.contains("搜索结果"));
    }

    @Test
    void searchWebWithCustomResultCount() {
        WebSearchTool webSearchTool = new WebSearchTool(apiKey);
        String query = "Java Spring Boot 最佳实践";
        // 测试自定义结果数量 (2个结果, 3个问题)
        String res = webSearchTool.searchWebCustom(query, 2, 3);
        System.out.println("=== 自定义结果数量测试 (2个结果, 3个问题) ===");
        System.out.println(res);
        assertNotNull(res);

        // 计算结果数量
        int resultCount = countOccurrences(res, "链接:");
        assertEquals(2, resultCount, "应该返回2个搜索结果");
    }

    @Test
    void searchWebWithNoRelatedQuestions() {
        WebSearchTool webSearchTool = new WebSearchTool(apiKey);
        String query = "微服务架构设计模式";
        // 测试无相关问题 (5个结果, 0个问题)
        String res = webSearchTool.searchWebCustom(query, 5, 0);
        System.out.println("=== 无相关问题测试 (5个结果, 0个问题) ===");
        System.out.println(res);
        assertNotNull(res);

        // 验证没有相关问题部分
        assertFalse(res.contains("相关问题"), "不应该包含相关问题部分");
    }

    @Test
    void searchWebWithMaxValues() {
        WebSearchTool webSearchTool = new WebSearchTool(apiKey);
        String query = "人工智能发展历史";
        // 测试最大值 (10个结果, 5个问题)
        String res = webSearchTool.searchWebCustom(query, 10, 5);
        System.out.println("=== 最大值测试 (10个结果, 5个问题) ===");
        System.out.println(res);
        assertNotNull(res);

        // 计算结果数量 (可能少于10个，取决于API实际返回)
        int resultCount = countOccurrences(res, "链接:");
        assertTrue(resultCount <= 10, "结果数量应该不超过10个");

        // 如果有相关问题，计算问题数量
        if (res.contains("相关问题")) {
            int questionCount = countOccurrences(res, "Q: ");
            assertTrue(questionCount <= 5, "问题数量应该不超过5个");
        }
    }

    @Test
    void searchWebWithInvalidParams() {
        WebSearchTool webSearchTool = new WebSearchTool(apiKey);
        String query = "云原生应用";
        // 测试无效参数 (应该使用默认值)
        String res = webSearchTool.searchWebCustom(query, -1, 12);
        System.out.println("=== 无效参数测试 (应使用默认值) ===");
        System.out.println(res);
        assertNotNull(res);

        // 应该使用默认值 (5个结果)
        int resultCount = countOccurrences(res, "链接:");
        assertTrue(resultCount <= 5, "结果数量应该不超过5个");

        // 如果有相关问题，应该使用默认值 (3个问题)
        if (res.contains("相关问题")) {
            int questionCount = countOccurrences(res, "Q: ");
            assertTrue(questionCount <= 3, "问题数量应该不超过3个");
        }
    }

    // 辅助方法：计算字符串中特定子串出现的次数
    private int countOccurrences(String text, String substring) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(substring, index)) != -1) {
            count++;
            index += substring.length();
        }
        return count;
    }
}
