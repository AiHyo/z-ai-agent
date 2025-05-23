package com.aih.zaiagent.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import cn.hutool.http.HttpRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


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
     * 执行网页搜索
     * @param query 搜索关键词
     * @return JSON格式的搜索结果
     */
    @Tool(description = "Search for information from Bing Search Engine")
    public String searchWeb(@ToolParam(description = "Search query keyword")String query) {
        // System.out.println("apiKey = " + apiKey);
        return HttpRequest.get(API_URL)
                .form("engine", ENGINE)
                .form("q", query)
                .form("api_key", apiKey)
                .timeout(5000)  // 设置5秒超时
                .execute()
                .body();
    }

    // /**
    //  * 带自定义位置的搜索
    //  * @param query 搜索关键词
    //  * @param location 自定义位置
    //  * @return JSON格式的搜索结果
    //  */
    // public String searchWeb(String query, String location) {
    //     return HttpRequest.get(API_URL)
    //             .form("engine", ENGINE)
    //             .form("q", query)
    //             .form("location", location)
    //             .form("api_key", apiKey)
    //             .timeout(5000)
    //             .execute()
    //             .body();
    // }
}
