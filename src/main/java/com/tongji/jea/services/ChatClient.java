package com.tongji.jea.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tongji.jea.model.ChatMessage;

import java.util.List;

/**
 * 与阿里云Qwen模型交互的客户端
 * 通过DashScopeExecutor统一处理HTTP请求
 */
public class ChatClient {
    private final DashScopeExecutor executor;
    private final String model;

    /**
     * 构造函数
     * @param executor HTTP请求执行器（已包含API Key）
     * @param model 模型名称（如 "qwen-turbo"）
     */
    public ChatClient(DashScopeExecutor executor, String model) {
        this.executor = executor;
        this.model = model;
    }

    /**
     * 【新】支持多轮对话：接收对话历史，调用Qwen模型
     * @param history 对话历史，每条消息包含 role ("user"/"assistant"/"system") 和 content
     * @return 模型生成的回答文本
     * @throws Exception API调用或解析异常
     */
    public String ask(List<ChatMessage> history) throws Exception {
        if (history == null || history.isEmpty()) {
            throw new IllegalArgumentException("对话历史不能为空");
        }

        ObjectMapper mapper = executor.getObjectMapper();

        // 构建顶层 payload
        ObjectNode payload = mapper.createObjectNode();
        payload.put("model", model);

        // 构建 messages 数组
        ArrayNode messages = mapper.createArrayNode();
        for (ChatMessage msg : history) {
            ObjectNode messageNode = mapper.createObjectNode();
            messageNode.put("role", msg.getRole());
            messageNode.put("content", msg.getContent());
            messages.add(messageNode);
        }

        // 设置 input.messages
        ObjectNode input = mapper.createObjectNode();
        input.set("messages", messages);
        payload.set("input", input);

        // 设置 parameters
        ObjectNode parameters = mapper.createObjectNode();
        parameters.put("stream", false); // 非流式
        payload.set("parameters", parameters);

        // 调用 DashScope API
        String path = "/api/v1/services/aigc/text-generation/generation";
        var response = executor.executePost(path, payload);

        // 解析响应
        var output = response.get("output");
        if (output == null) {
            throw new IllegalStateException("API 响应缺少 'output' 字段");
        }

        var choices = output.get("choices");
        if (choices == null || choices.size() == 0) {
            throw new IllegalStateException("API 响应缺少有效 'choices'");
        }

        // 提取第一个 choice 的 content
        var firstChoice = choices.get(0);
        var message = firstChoice.get("message");
        if (message == null || !message.has("content")) {
            throw new IllegalStateException("模型回复缺少 'content'");
        }

        return message.get("content").asText();
    }
}