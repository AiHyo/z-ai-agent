package com.aih.zaiagent.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * 在终端执行命令
 * @author AiHyo
 */
public class TerminalOperationTool {

    @Tool(description = "Execute the command in the terminal")
    public String executeTerminalCommand(@ToolParam(description = "Commands executed in the terminal") String command) {
        StringBuilder output = new StringBuilder();
        try {
            // 1. windows
            // ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", command);
            // Process process = builder.start();
            // 2. mac / linux
            // Process process = Runtime.getRuntime().exec(command);
            // 根据操作系统类型选择不同的命令执行器
            ProcessBuilder builder;
            String osName = System.getProperty("os.name").toLowerCase();
            String charset;
            if (osName.contains("win")) {
                // windows
                charset = "GBK";
                builder = new ProcessBuilder("cmd.exe", "/c", command);
            } else {
                // mac / linux
                charset = "UTF-8";
                builder = new ProcessBuilder("/bin/sh", "-c", command);
            }
            Process process = builder.start();

            // 读取输出流
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), charset))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // 如果是Windows系统，尝试将GBK转换为UTF-8
                    if (osName.contains("win")) {
                        line = new String(line.getBytes(charset), StandardCharsets.UTF_8);
                    }
                    output.append(line).append("\n");
                }
            }

            // 读取错误流
            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream(), charset))) {
                String line;
                while ((line = errorReader.readLine()) != null) {
                    // 同样转换错误流的编码
                    if (osName.contains("win")) {
                        line = new String(line.getBytes(charset), StandardCharsets.UTF_8);
                    }
                    output.append(line).append("\n");
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                output.append("Command execution failed with exit code: ").append(exitCode);
            }
        } catch (IOException | InterruptedException e) {
            output.append("Error executing command: ").append(e.getMessage());
        }
        return output.toString();
    }
}
