package com.aih.zaiagent.demo.invoke;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//https://help.aliyun.com/zh/model-studio/use-qwen-by-calling-api#b1320a1664b9a
public class HttpAiInvoke {

    private static final String API_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";

    public static String callQwenApi(String userMessage) {
        // 构建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "qwen-plus");

        List<Map<String, String>> messages = new ArrayList<>();

        // 添加系统消息
        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", "You are a helpful assistant.");
        messages.add(systemMessage);

        // 添加用户消息
        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", userMessage);
        messages.add(userMsg);

        requestBody.put("messages", messages);

        // 发送HTTP请求
        HttpResponse response = HttpRequest.post(API_URL)
                .header("Authorization", "Bearer " + TestApiKey.API_KEY)
                .header("Content-Type", "application/json")
                .body(JSONUtil.toJsonStr(requestBody))
                .execute();

        // 解析响应
        if (response.isOk()) {
            JSONObject result = JSONUtil.parseObj(response.body());
            return result.getByPath("choices[0].message.content", String.class);
        } else {
            return "Error: " + response.getStatus() + " - " + response.body();
        }
    }

    public static void main(String[] args) {
        String userQuestion = "你是谁？";
        String response = callQwenApi(userQuestion);
        System.out.println("问题: " + userQuestion);
        System.out.println("回答: " + response);
    }
}
