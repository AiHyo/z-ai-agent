package com.aih.zaiagent.tools.registration;

import com.aih.zaiagent.tools.*;
import jakarta.annotation.Resource;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbacks;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 工具注册类
 * @author AiHyo
 */
@Configuration
public class ToolRegistration {

    @Value("${search-api.api-key}")
    private String apiKey;

    @Resource
    private FileOperationTool fileOperationTool;
    @Resource
    private PDFGenerationTool pdfGenerationTool;
    @Resource
    private ResourceDownloadTool resourceDownloadTool;

    @Bean
    public ToolCallback[] availableTools() {
        TerminalOperationTool terminalOperationTool = new TerminalOperationTool();
        WebSearchTool webSearchTool = new WebSearchTool(apiKey);
        WebSpiderTool webSpiderTool = new WebSpiderTool();
        TerminateTool terminateTool = new TerminateTool();
        // 注册所有工具
        return ToolCallbacks.from(
                fileOperationTool,
                pdfGenerationTool,
                resourceDownloadTool,
                terminalOperationTool,
                webSearchTool,
                webSpiderTool,
                terminateTool
        );
    }
}
