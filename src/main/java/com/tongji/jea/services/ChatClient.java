package com.tongji.jea.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tongji.jea.services.api.IChatClient;


/**
 * 与阿里云Qwen模型交互的客户端
 * 通过DashScopeExecutor统一处理HTTP请求
 */
public class ChatClient implements IChatClient {
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
     * 向Qwen模型发送问题并获取回答
     * @param question 用户问题
     * @return 模型生成的回答文本
     * @throws Exception API调用或解析异常
     */
    @Override
    public String ask(String question) throws Exception {
        ObjectMapper mapper = executor.getObjectMapper();
        ObjectNode payload = mapper.createObjectNode();
        payload.put("model", model);

        ObjectNode input = mapper.createObjectNode();
        input.put("prompt", question);
        payload.set("input", input);

        ObjectNode parameters = mapper.createObjectNode();
        parameters.put("stream", false); // 非流式输出
        payload.set("parameters", parameters);

        // 调用阿里云API（路径与阿里云文档一致）
        String path = "/api/v1/services/aigc/text-generation/generation";
        JsonNode response = executor.executePost(path, payload);

        // 解析响应（兼容阿里云API结构）
        JsonNode output = response.get("output");
        if (output == null) {
            throw new IllegalStateException("Response missing 'output'");
        }

        JsonNode choices = output.get("choices");
        if (choices == null || choices.isEmpty()) {
            throw new IllegalStateException("Response missing 'choices'");
        }

        // 获取第一个回复内容
        return choices.get(0).get("message").get("content").asText();
    }
}