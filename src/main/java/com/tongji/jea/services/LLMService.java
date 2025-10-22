package com.tongji.jea.services;
import org.jetbrains.annotations.NotNull;

public class LLMService {

    private final AliyunLLMClient client;

    public LLMService() {
        this.client = new AliyunLLMClient();
    }

    public String ask(@NotNull String question) {
        // 未来：可插入 RAG 逻辑
        return client.askLLM(question);
    }
}
