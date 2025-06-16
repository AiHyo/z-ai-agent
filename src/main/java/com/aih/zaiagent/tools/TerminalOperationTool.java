package com.aih.zaiagent.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.io.File;

/**
 * 在终端执行命令
 * @author AiHyo
 */
public class TerminalOperationTool {

    @Tool(description = "Execute terminal commands")
    public String executeTerminalCommand(@ToolParam(description = "Command to execute") String command) {
        try {
            Process process;
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                // Windows系统
                process = Runtime.getRuntime().exec(new String[]{"cmd.exe", "/c", command});
            } else {
                // Linux/Mac系统
                process = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", command});
            }

            // 使用正确的字符编码读取输出
            String charset = System.getProperty("os.name").toLowerCase().contains("win") ? "GBK" : "UTF-8";

            // 读取标准输出
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream(), charset));
            // 读取错误输出
            BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream(), charset));

            StringBuilder output = new StringBuilder();
            String line;
            while ((line = stdInput.readLine()) != null) {
                output.append(line).append("\n");
            }

            // 添加错误输出
            while ((line = stdError.readLine()) != null) {
                output.append("ERROR: ").append(line).append("\n");
            }

            // 等待进程完成
            int exitCode = process.waitFor();
            output.append("\n执行完成，退出代码: ").append(exitCode);

            return output.toString();
        } catch (Exception e) {
            return "执行命令出错: " + e.getMessage();
        }
    }
}
