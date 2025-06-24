package com.aih.zaiagent.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import cn.hutool.http.HttpRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.json.JSONArray;

/**
 * 网页搜索工具（提供搜索功能）
 * @author AiHyo
 */
public class WebSearchTool {
    private static final String API_URL = "https://www.searchapi.io/api/v1/search";
    private static final String ENGINE = "bing";

    private final String apiKey;

    public WebSearchTool(String apiKey) {
        this.apiKey = apiKey;
    }
    /**
     * 执行网页搜索（带自定义参数）
     * @param query 搜索关键词
     * @param maxResults 最大结果数量
     * @param maxQuestions 最大相关问题数量
     * @return JSON格式的搜索结果
     */
    @Tool(name = "searchWebCustom", description = "Search for information from Bing Search Engine with custom parameters. Use this tool when you need to find up-to-date information with specific result count.")
    public String searchWebCustom(
        @ToolParam(description = "Search query keyword. Should be specific and concise.") String query,
        @ToolParam(description = "Maximum number of search results to return (1-10). Default is 5.") Integer maxResults,
        @ToolParam(description = "Maximum number of related questions to return (0-5). Default is 3.") Integer maxQuestions
    ) {
        try {
            // 参数验证和默认值设置
            int resultCount = (maxResults != null && maxResults > 0 && maxResults <= 10) ? maxResults : 5;
            int questionCount = (maxQuestions != null && maxQuestions >= 0 && maxQuestions <= 5) ? maxQuestions : 3;

            String response = HttpRequest.get(API_URL)
                    .form("engine", ENGINE)
                    .form("q", query)
                    .form("api_key", apiKey)
                    .timeout(30000) // 增加到30秒
                    .execute()
                    .body();

            // 解析JSON
            JSONObject jsonResponse = JSONUtil.parseObj(response);
            StringBuilder formattedResults = new StringBuilder();

            // 添加搜索元数据
            if (jsonResponse.containsKey("search_information")) {
                JSONObject searchInfo = jsonResponse.getJSONObject("search_information");
                // formattedResults.append("搜索查询: ").append(searchInfo.getStr("query_displayed", query)).append("\n");
            }

            // 添加有机搜索结果
            if (jsonResponse.containsKey("organic_results")) {
                formattedResults.append("## 搜索结果\n\n");
                JSONArray results = jsonResponse.getJSONArray("organic_results");
                int count = Math.min(results.size(), resultCount);

                for (int i = 0; i < count; i++) {
                    JSONObject result = results.getJSONObject(i);
                    formattedResults.append(i+1).append(". **").append(result.getStr("title", "无标题")).append("**\n");
                    formattedResults.append("   ").append(result.getStr("snippet", "无描述")).append("\n");
                    formattedResults.append("   链接: ").append(result.getStr("link", "无链接")).append("\n\n");
                }
            }

            // 添加本地结果（如果有机结果为空）
            if (formattedResults.length() == 0 && jsonResponse.containsKey("local_results") &&
                !jsonResponse.getJSONArray("local_results").isEmpty()) {

                formattedResults.append("## 本地景点\n\n");
                JSONArray localResults = jsonResponse.getJSONArray("local_results");
                int count = Math.min(localResults.size(), resultCount);

                for (int i = 0; i < count; i++) {
                    JSONObject result = localResults.getJSONObject(i);
                    formattedResults.append(i+1).append(". **").append(result.getStr("title", "无标题")).append("**\n");
                    if (result.containsKey("type")) {
                        formattedResults.append("   类型: ").append(result.getStr("type", "")).append("\n");
                    }
                    if (result.containsKey("address")) {
                        formattedResults.append("   地址: ").append(result.getStr("address", "")).append("\n");
                    }
                    if (result.containsKey("phone")) {
                        formattedResults.append("   电话: ").append(result.getStr("phone", "")).append("\n");
                    }
                    if (result.containsKey("website")) {
                        formattedResults.append("   网站: ").append(result.getStr("website", "")).append("\n");
                    }
                    formattedResults.append("\n");
                }
            }

            // 添加相关问题（如果需要）
            if (questionCount > 0 && jsonResponse.containsKey("related_questions") &&
                !jsonResponse.getJSONArray("related_questions").isEmpty()) {
                formattedResults.append("## 相关问题\n\n");
                JSONArray relatedQuestions = jsonResponse.getJSONArray("related_questions");
                int count = Math.min(relatedQuestions.size(), questionCount);

                for (int i = 0; i < count; i++) {
                    JSONObject question = relatedQuestions.getJSONObject(i);
                    formattedResults.append("Q: ").append(question.getStr("question", "")).append("\n");
                    formattedResults.append("A: ").append(question.getStr("answer", "")).append("\n\n");
                }
            }

            // 如果没有结果
            if (formattedResults.length() == 0) {
                return "未找到与 \"" + query + "\" 相关的搜索结果。请尝试使用不同的关键词。";
            }

            return formattedResults.toString();
        } catch (Exception e) {
            return "搜索过程中发生错误: " + e.getMessage();
        }
    }

    /**
     * 执行网页搜索（使用默认参数）
     * @param query 搜索关键词
     * @return JSON格式的搜索结果
     */
    @Tool(name = "searchWeb", description = "Search for information from Bing Search Engine with default settings (5 results, 3 questions).")
    public String searchWeb(@ToolParam(description = "Search query keyword") String query) {
        return searchWebCustom(query, 5, 3);
    }
}
