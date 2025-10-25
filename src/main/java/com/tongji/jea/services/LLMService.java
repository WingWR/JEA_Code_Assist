package com.tongji.jea.services;
import org.jetbrains.annotations.NotNull;
/**
 * 与大语言模型（LLM）交互的服务类
 * 即所有输入（资料、代码上下文、问题）在此处集成处理
 */
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
