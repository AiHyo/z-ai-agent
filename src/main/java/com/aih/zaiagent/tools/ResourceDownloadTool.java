package com.aih.zaiagent.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.aih.zaiagent.service.TencentCosService;
import jakarta.annotation.Resource;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * 资源下载工具（提供下载功能）
 * @author AiHyo
 */
@Component
public class ResourceDownloadTool {

    @Resource
    private TencentCosService tencentCosService;

    private static final String COS_DIRECTORY = "download";

    @Tool(description = "Download a resource (image, document, etc.) from a URL and save it to cloud storage. Use this when you need to save external resources.")
    public String downloadResource(
            @ToolParam(description = "URL of the resource to download. Must be a direct link to the file.") String url,
            @ToolParam(description = "Name to save the file as, including extension (e.g., 'image.jpg', 'document.pdf')") String fileName) {
        try {
            // 验证URL
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                return "Error: URL must start with http:// or https://";
            }

            // 验证文件名
            if (fileName == null || fileName.trim().isEmpty()) {
                // 从URL中提取文件名
                fileName = url.substring(url.lastIndexOf('/') + 1);
                if (fileName.isEmpty() || fileName.contains("?")) {
                    fileName = "downloaded_file_" + System.currentTimeMillis() + getExtensionFromUrl(url);
                }
            }

            // 创建临时文件
            File tempFile = File.createTempFile("download", ".tmp");

            // 设置超时和进度监控
            HttpResponse response = HttpRequest.get(url)
                    .timeout(30000)  // 30秒超时
                    .execute();

            if (response.getStatus() != 200) {
                return "Error: Failed to download resource. HTTP status: " + response.getStatus();
            }

            // 保存到临时文件
            FileUtil.writeBytes(response.bodyBytes(), tempFile);

            // 检查文件大小
            long fileSize = tempFile.length();
            if (fileSize == 0) {
                tempFile.delete();
                return "Error: Downloaded file is empty";
            }

            if (fileSize > 50 * 1024 * 1024) { // 50MB限制
                tempFile.delete();
                return "Error: File too large (> 50MB)";
            }

            // 上传到腾讯云COS
            String cosUrl = tencentCosService.uploadFile(fileName, tempFile, COS_DIRECTORY);
            String cosKey = COS_DIRECTORY + "/" + fileName;

            // 删除临时文件
            tempFile.delete();

            return "Resource downloaded successfully.\nFile name: " + fileName +
                   "\nFile size: " + formatFileSize(fileSize) +
                   "\nAccess URL: " + cosUrl +
                   "\nStorage key: " + cosKey;
        } catch (Exception e) {
            return "Error downloading resource: " + e.getMessage();
        }
    }

    private String getExtensionFromUrl(String url) {
        String extension = "";
        int dotIndex = url.lastIndexOf('.');
        int queryIndex = url.indexOf('?', dotIndex);
        if (dotIndex > 0) {
            if (queryIndex > dotIndex) {
                extension = url.substring(dotIndex, queryIndex);
            } else {
                extension = url.substring(dotIndex);
            }
        }
        return extension.length() > 0 ? extension : ".bin";
    }

    private String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        }
        int z = (63 - Long.numberOfLeadingZeros(size)) / 10;
        return String.format("%.1f %sB", (double)size / (1L << (z*10)), " KMGTPE".charAt(z));
    }
}
