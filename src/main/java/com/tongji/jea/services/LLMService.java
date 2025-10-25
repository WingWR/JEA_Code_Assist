package com.tongji.jea.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * 与大语言模型（LLM）交互的服务类
 * 负责封装 DashScope 调用逻辑
 * 可以后续扩展为支持多模型或RAG增强
 */
public class LLMService {

    /** DashScope 执行器 */
    private final DashScopeExecutor executor;

    /** JSON 处理器 */
    private final ObjectMapper mapper;

    /** 通义千问模型路径（可以换为 chat/completions 接口） */
    private static final String CHAT_PATH = "/api/v1/services/aigc/text-generation/generation";

    public LLMService(@NotNull String apiKey) {
        this.executor = new DashScopeExecutor(apiKey);
        this.mapper = executor.getObjectMapper();
    }

    /**
     * 向 LLM 提问
     * @param question 用户问题
     * @return LLM 的回答（文本）
     */
    public String ask(@NotNull String question) {
        try {
            // ===== 1️⃣ 构造 JSON 请求体 =====
            JsonNode request = mapper.createObjectNode()
                    .put("model", "qwen-turbo") // 可换为 qwen-plus / qwen-max 等
                    .set("input", mapper.createObjectNode()
                            .set("messages", mapper.createArrayNode()
                                    .add(mapper.createObjectNode()
                                            .put("role", "system")
                                            .put("content", "You are JEA, an intelligent Java assistant. Answer clearly and concisely."))
                                    .add(mapper.createObjectNode()
                                            .put("role", "user")
                                            .put("content", question))
                            )
                    );

            // ===== 2️⃣ 调用 DashScope POST =====
            JsonNode response = executor.executePost(CHAT_PATH, request);

            // ===== 3️⃣ 解析响应 =====
            // DashScope 格式: data.output.text 或 data.output.choices[0].message.content
            if (response == null) {
                return "[Error] Empty response from LLM.";
            }

            JsonNode outputNode = response.path("output").path("text");
            if (outputNode.isMissingNode()) {
                // 兼容其他结构
                JsonNode choices = response.path("output").path("choices");
                if (choices.isArray() && choices.size() > 0) {
                    return choices.get(0).path("message").path("content").asText("[No content]");
                }
                return "[Error] Unexpected response format: " + response.toPrettyString();
            }

            return outputNode.asText();

        } catch (IOException e) {
            return "[Network Error] " + e.getMessage();
        } catch (Exception e) {
            return "[Unexpected Error] " + e.getMessage();
        }
    }

    /** 手动关闭资源 */
    public void close() {
        executor.close();
    }
}
