package com.aih.zaiagent.tools;

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

    @Tool(description = "Download a resource from a given URL")
    public String downloadResource(@ToolParam(description = "URL of the resource to download") String url,
                                   @ToolParam(description = "Name of the file to save the downloaded resource") String fileName) {
        try {
            // 创建临时文件
            File tempFile = File.createTempFile("download", ".tmp");
            
            // 使用 Hutool 的 downloadFile 方法下载资源到临时文件
            HttpUtil.downloadFile(url, tempFile);

            // 上传到腾讯云COS
            String cosUrl = tencentCosService.uploadFile(fileName, tempFile, COS_DIRECTORY);
            String cosKey = COS_DIRECTORY + "/" + fileName;
            
            // 删除临时文件
            tempFile.delete();

            return "Resource downloaded successfully. Access URL: " + cosUrl + ", Key: " + cosKey;
        } catch (Exception e) {
            return "Error downloading resource: " + e.getMessage();
        }
    }
}
