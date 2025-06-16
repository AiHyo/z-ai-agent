package com.aih.zaiagent.tools;

import com.aih.zaiagent.service.TencentCosService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 工具综合测试类
 * 使用 @TestMethodOrder 确保测试按指定顺序执行
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("工具综合测试")
public class ToolsComprehensiveTest {

    @Value("${search-api.api-key}")
    private String apiKey;

    // 各种工具实例
    private WebSearchTool webSearchTool;
    private WebSpiderTool webSpiderTool;
    private TerminalOperationTool terminalOperationTool;
    private ResourceDownloadTool resourceDownloadTool;
    private FileOperationTool fileOperationTool;
    private PDFGenerationTool pdfGenerationTool;

    // 添加依赖注入
    @Resource
    private TencentCosService tencentCosService;

    /**
     * 在所有测试开始前初始化工具实例
     */
    @BeforeEach
    void setup() {
        if (webSearchTool == null) {
            webSearchTool = new WebSearchTool(apiKey);
        }
        if (webSpiderTool == null) {
            webSpiderTool = new WebSpiderTool();
        }
        if (terminalOperationTool == null) {
            terminalOperationTool = new TerminalOperationTool();
        }
        if (resourceDownloadTool == null) {
            resourceDownloadTool = new ResourceDownloadTool();
            // 如果ResourceDownloadTool需要tencentCosService，需要手动设置
            try {
                java.lang.reflect.Field field = ResourceDownloadTool.class.getDeclaredField("tencentCosService");
                field.setAccessible(true);
                field.set(resourceDownloadTool, tencentCosService);
            } catch (Exception e) {
                System.err.println("无法设置tencentCosService: " + e.getMessage());
            }
        }
        if (fileOperationTool == null) {
            fileOperationTool = new FileOperationTool();
            // 为FileOperationTool设置tencentCosService
            try {
                java.lang.reflect.Field field = FileOperationTool.class.getDeclaredField("tencentCosService");
                field.setAccessible(true);
                field.set(fileOperationTool, tencentCosService);
            } catch (Exception e) {
                System.err.println("无法设置tencentCosService: " + e.getMessage());
            }
        }
        if (pdfGenerationTool == null) {
            pdfGenerationTool = new PDFGenerationTool();
            // 如果PDFGenerationTool需要tencentCosService，也需要设置
        }
    }

    // ==================== WebSearchTool 测试 ====================

    @Test
    @Order(1)
    @DisplayName("测试1: WebSearchTool - 默认参数搜索")
    void testWebSearchDefault() {
        System.out.println("\n========== 测试1: WebSearchTool - 默认参数搜索 ==========");

        String query = "spring ai的工具调用";
        System.out.println("搜索查询: " + query);

        String result = webSearchTool.searchWeb(query);
        System.out.println("搜索结果:");
        System.out.println(result);

        // 验证结果
        assertNotNull(result, "搜索结果不应为空");
        assertTrue(result.contains("搜索查询"), "结果应包含'搜索查询'部分");
        assertTrue(result.contains("搜索结果"), "结果应包含'搜索结果'部分");

        System.out.println("测试1完成 ✓");
    }

    @Test
    @Order(2)
    @DisplayName("测试2: WebSearchTool - 自定义结果数量")
    void testWebSearchCustom() {
        System.out.println("\n========== 测试2: WebSearchTool - 自定义结果数量 ==========");

        String query = "Java Spring Boot 最佳实践";
        int maxResults = 2;
        int maxQuestions = 3;
        System.out.println("搜索查询: " + query);
        System.out.println("最大结果数: " + maxResults + ", 最大问题数: " + maxQuestions);

        String result = webSearchTool.searchWebCustom(query, maxResults, maxQuestions);
        System.out.println("搜索结果:");
        System.out.println(result);

        // 验证结果
        assertNotNull(result, "搜索结果不应为空");

        int resultCount = countOccurrences(result, "链接:");
        System.out.println("结果数量: " + resultCount);
        assertEquals(maxResults, resultCount, "结果数量应为" + maxResults + "个");

        System.out.println("测试2完成 ✓");
    }

    @Test
    @Order(3)
    @DisplayName("测试3: WebSearchTool - 无相关问题")
    void testWebSearchNoQuestions() {
        System.out.println("\n========== 测试3: WebSearchTool - 无相关问题 ==========");

        String query = "微服务架构设计模式";
        int maxResults = 5;
        int maxQuestions = 0;
        System.out.println("搜索查询: " + query);
        System.out.println("最大结果数: " + maxResults + ", 最大问题数: " + maxQuestions);

        String result = webSearchTool.searchWebCustom(query, maxResults, maxQuestions);
        System.out.println("搜索结果:");
        System.out.println(result);

        // 验证结果
        assertNotNull(result, "搜索结果不应为空");

        // 验证没有相关问题部分
        assertFalse(result.contains("相关问题"), "结果不应包含'相关问题'部分");

        System.out.println("测试3完成 ✓");
    }

    // ==================== WebSpiderTool 测试 ====================

    @Test
    @Order(4)
    @DisplayName("测试4: WebSpiderTool - 网页抓取")
    void testWebSpider() {
        System.out.println("\n========== 测试4: WebSpiderTool - 网页抓取 ==========");

        String url = "https://www.baidu.com/";
        System.out.println("抓取URL: " + url);

        String result = webSpiderTool.scrapeWebPage(url);
        System.out.println("抓取结果 (前500字符):");
        System.out.println(result.length() > 500 ? result.substring(0, 500) + "..." : result);

        // 验证结果
        assertNotNull(result, "抓取结果不应为空");
        assertTrue(result.length() > 0, "抓取结果应有内容");

        System.out.println("测试4完成 ✓");
    }

    @Test
    @Order(4)
    @DisplayName("测试4.1: WebSpiderTool - 网页分析")
    void testWebSpiderAnalyze() {
        System.out.println("\n========== 测试4.1: WebSpiderTool - 网页分析 ==========");

        String url = "https://spring.io/projects/spring-ai";
        System.out.println("分析URL: " + url);

        String result = webSpiderTool.analyzeWebPage(url);
        System.out.println("分析结果 (前1000字符):");
        System.out.println(result.length() > 1000 ? result.substring(0, 1000) + "..." : result);

        // 验证结果
        assertNotNull(result, "分析结果不应为空");
        assertTrue(result.contains("网页分析"), "结果应包含'网页分析'部分");

        System.out.println("测试4.1完成 ✓");
    }

    // ==================== TerminalOperationTool 测试 ====================

    @Test
    @Order(5)
    @DisplayName("测试5: TerminalOperationTool - 终端命令执行")
    void testTerminalOperation() {
        System.out.println("\n========== 测试5: TerminalOperationTool - 终端命令执行 ==========");

        // 使用跨平台命令
        String command = System.getProperty("os.name").toLowerCase().contains("win") ? "dir" : "ls -la";
        System.out.println("执行命令: " + command);

        String result = terminalOperationTool.executeTerminalCommand(command);
        System.out.println("执行结果:");
        System.out.println(result);

        // 验证结果
        assertNotNull(result, "执行结果不应为空");
        assertTrue(result.length() > 0, "执行结果应有内容");

        System.out.println("测试5完成 ✓");
    }

    // ==================== ResourceDownloadTool 测试 ====================

    @Test
    @Order(6)
    @DisplayName("测试6: ResourceDownloadTool - 资源下载")
    void testResourceDownload() {
        System.out.println("\n========== 测试6: ResourceDownloadTool - 资源下载 ==========");

        // 使用稳定的小文件URL
        String url = "https://www.baidu.com/img/flexible/logo/pc/result.png";
        String fileName = "baidu_logo.png";
        System.out.println("下载URL: " + url);
        System.out.println("保存为: " + fileName);

        String result = resourceDownloadTool.downloadResource(url, fileName);
        System.out.println("下载结果:");
        System.out.println(result);

        // 验证结果
        assertNotNull(result, "下载结果不应为空");
        assertTrue(result.contains("Resource downloaded successfully") || result.contains("下载成功"),
                "下载应该成功");

        System.out.println("测试6完成 ✓");
    }

    // ==================== FileOperationTool 测试 ====================

    @Test
    @Order(7)
    @DisplayName("测试7: FileOperationTool - 文件写入")
    void testFileWrite() {
        System.out.println("\n========== 测试7: FileOperationTool - 文件写入 ==========");

        String fileName = "test_file.txt";
        String content = "这是一个测试文件内容，用于测试FileOperationTool的写入功能。\n" +
                         "当前时间: " + java.time.LocalDateTime.now();
        System.out.println("写入文件: " + fileName);
        System.out.println("文件内容: " + content);

        String result = fileOperationTool.writeFile(fileName, content);
        System.out.println("写入结果:");
        System.out.println(result);

        // 验证结果
        assertNotNull(result, "写入结果不应为空");
        assertTrue(result.contains("success") || result.contains("成功"), "写入应该成功");

        System.out.println("测试7完成 ✓");
    }

    @Test
    @Order(8)
    @DisplayName("测试8: FileOperationTool - 文件读取")
    void testFileRead() {
        System.out.println("\n========== 测试8: FileOperationTool - 文件读取 ==========");

        String fileName = "test_file.txt";
        System.out.println("读取文件: " + fileName);

        String result = fileOperationTool.readFile(fileName);
        System.out.println("读取结果:");
        System.out.println(result);

        // 验证结果
        assertNotNull(result, "读取结果不应为空");
        assertTrue(result.contains("测试文件内容"), "应该能读取到之前写入的内容");

        System.out.println("测试8完成 ✓");
    }

    // ==================== PDFGenerationTool 测试 ====================

    @Test
    @Order(9)
    @DisplayName("测试9: PDFGenerationTool - PDF生成")
    void testPDFGeneration() {
        System.out.println("\n========== 测试9: PDFGenerationTool - PDF生成 ==========");

        String fileName = "test_pdf.pdf";
        String content = "# 测试PDF文档\n\n" +
                         "这是一个使用Markdown格式生成的PDF文档。\n\n" +
                         "## 功能测试\n\n" +
                         "- 支持Markdown格式\n" +
                         "- 支持中文字符\n" +
                         "- 生成PDF文件\n\n" +
                         "当前时间: " + java.time.LocalDateTime.now();
        System.out.println("生成PDF: " + fileName);
        System.out.println("PDF内容: " + content);

        String result = pdfGenerationTool.generatePDF(fileName, content);
        System.out.println("生成结果:");
        System.out.println(result);

        // 验证结果
        assertNotNull(result, "生成结果不应为空");
        assertTrue(result.contains("PDF generated successfully") || result.contains("PDF生成成功"),
                "PDF生成应该成功");

        System.out.println("测试9完成 ✓");
    }

    // ==================== 辅助方法 ====================

    /**
     * 计算字符串中特定子串出现的次数
     */
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
