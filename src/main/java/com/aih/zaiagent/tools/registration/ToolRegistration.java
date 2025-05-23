package com.aih.zaiagent.tools.registration;

import com.aih.zaiagent.tools.*;
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

    @Bean
    public ToolCallback[] allTools() {
        FileOperationTool fileOperationTool = new FileOperationTool();
        PDFGenerationTool pdfGenerationTool = new PDFGenerationTool();
        ResourceDownloadTool resourceDownloadTool = new ResourceDownloadTool();
        TerminalOperationTool terminalOperationTool = new TerminalOperationTool();
        WebSearchTool webSearchTool = new WebSearchTool(apiKey);
        WebSpiderTool webSpiderTool = new WebSpiderTool();
        // 注册所有工具
        return ToolCallbacks.from(
                fileOperationTool,
                pdfGenerationTool,
                resourceDownloadTool,
                terminalOperationTool,
                webSearchTool,
                webSpiderTool
        );
    }
}
