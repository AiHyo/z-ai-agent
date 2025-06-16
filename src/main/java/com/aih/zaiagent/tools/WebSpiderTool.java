package com.aih.zaiagent.tools;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.IOException;

/**
 * 网页爬虫解析
 * @author AiHyo
 */
public class WebSpiderTool {


    @Tool(description = "scrape the source code of web page")
    public String scrapeWebPage(@ToolParam(description = "URL of the web page to scrape") String url) {
        try {
            Document doc = Jsoup.connect(url).get();
            return doc.html();
        } catch (IOException e) {
            return "Error scraping web page: " + e.getMessage();
        }
    }


    /**
     * 智能网页内容分析
     * 返回网页内容，并让AI自行决定如何处理
     */
    @Tool(name = "analyzeWebPage", description = "Analyze a web page and extract useful information based on the content structure")
    public String analyzeWebPage(@ToolParam(description = "URL of the web page to analyze") String url) {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .timeout(30000)
                    .get();

            // 获取页面标题
            String title = doc.title();

            // 获取页面文本内容（不含HTML标签）
            String text = doc.text();

            // 获取主要内容（尝试识别主要文章内容）
            String mainContent = doc.select("article, .content, .main, #content, #main").text();
            if (mainContent.isEmpty()) {
                mainContent = "未能识别主要内容区域";
            }

            // 获取所有链接
            StringBuilder links = new StringBuilder();
            doc.select("a[href]").stream()
               .limit(10)  // 限制链接数量
               .forEach(link -> links.append("- ").append(link.text()).append(": ").append(link.attr("abs:href")).append("\n"));

            // 获取所有图片
            StringBuilder images = new StringBuilder();
            doc.select("img[src]").stream()
               .limit(5)  // 限制图片数量
               .forEach(img -> images.append("- ").append(img.attr("alt")).append(": ").append(img.attr("abs:src")).append("\n"));

            // 构建响应
            StringBuilder response = new StringBuilder();
            response.append("# 网页分析: ").append(title).append("\n\n");
            response.append("URL: ").append(url).append("\n\n");

            response.append("## 页面概览\n");
            response.append(text.length() > 300 ? text.substring(0, 300) + "..." : text).append("\n\n");

            response.append("## 主要内容\n");
            response.append(mainContent.length() > 1000 ? mainContent.substring(0, 1000) + "..." : mainContent).append("\n\n");

            response.append("## 页面链接\n");
            response.append(links.length() > 0 ? links.toString() : "未找到链接").append("\n");

            response.append("## 页面图片\n");
            response.append(images.length() > 0 ? images.toString() : "未找到图片").append("\n");

            // 添加HTML源代码（截断版本）
            response.append("## HTML源代码（截断）\n```html\n");
            String html = doc.html();
            response.append(html.length() > 2000 ? html.substring(0, 2000) + "..." : html).append("\n```\n\n");

            response.append("请根据以上信息分析网页内容，提取有用信息。如需更详细的内容，请指定要提取的部分。");

            return response.toString();
        } catch (IOException e) {
            return "Error analyzing web page: " + e.getMessage();
        }
    }


}
