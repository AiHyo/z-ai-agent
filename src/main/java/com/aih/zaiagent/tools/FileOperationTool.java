package com.aih.zaiagent.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import jakarta.annotation.Resource;

import com.aih.zaiagent.service.TencentCosService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * 文件操作工具（提供上传下载功能）
 * @author AiHyo
 */
@Component
public class FileOperationTool {

    private final String COS_DIRECTORY = "file";

    @Resource
    private TencentCosService tencentCosService;

    @Tool(description = "Read content from a file")
    public String readFile(String fileName) {
        try {
            // 直接从腾讯云COS读取文件
            String key = COS_DIRECTORY + "/" + fileName;
            URL url = tencentCosService.getTemporaryUrl(key);
            try (InputStream input = url.openStream()) {
                return IoUtil.read(input, StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            return "Error reading file: " + e.getMessage();
        }
    }

    @Tool(description = "Write content to a file")
    public String writeFile(@ToolParam(description = "Name of the file to write") String fileName,
                            @ToolParam(description = "Content to write to the file") String content) {
        try {
            // 创建临时文件
            File tempFile = File.createTempFile("temp", ".tmp");
            FileUtil.writeUtf8String(content, tempFile);

            // 上传到腾讯云COS
            String fileUrl = tencentCosService.uploadFile(fileName, tempFile, COS_DIRECTORY);

            // 删除临时文件
            tempFile.delete();

            return "File written successfully. Access URL: " + fileUrl;
        } catch (Exception e) {
            return "Error writing to file: " + e.getMessage();
        }
    }
}
