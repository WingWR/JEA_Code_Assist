package com.tongji.jea.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.List;

/**
 * EmbeddingClient
 * ----------------
 * 负责调用阿里云 DashScope 的文本向量化（Text Embedding）接口。
 * 依赖 DashScopeExecutor 执行 HTTP 通信。
 */
public class EmbeddingHttpClient {
    private final DashScopeExecutor executor;
    private final String model;

    /**
     *
     * @param executor
     * @param model
     */
    public EmbeddingHttpClient(DashScopeExecutor executor, String model) {
        this.executor = executor;
        this.model = model;
    }

    public List<Double> getEmbedding(String text) throws Exception{
        if(text == null || text.isEmpty()){
            throw new IllegalArgumentException("text cannot be null or empty");
        }

        //构造input结构 ： { "input": { "texts": [text] } }
        ObjectNode inputNode = executor.getObjectMapper().createObjectNode();
        inputNode.putArray("texts").add(text);

        ObjectNode payload = executor.getObjectMapper().createObjectNode();
        payload.put("model", model);
        payload.set("input", inputNode);

        //构造embedding模型api路径
        String path = "/api/v1/services/embeddings/text-embedding/text-embedding/" + model;

        JsonNode response = executor.executePost(path, payload);

        JsonNode output = response.path("output");
        JsonNode embeddings = output.path("embeddings");

        if (!embeddings.isArray() || embeddings.isEmpty()) {
            throw new IllegalStateException("Invalid response: missing 'output.embeddings'.");
        }

        JsonNode firstEmbedding = embeddings.get(0).path("embedding");
        if (!firstEmbedding.isArray() || firstEmbedding.isEmpty()) {
            throw new IllegalStateException("Invalid response: missing 'embedding' array.");
        }

        List<Double> result = new ArrayList<>(firstEmbedding.size());
        for (int i = 0; i < firstEmbedding.size(); i++) {
            result.add(firstEmbedding.get(i).asDouble());
        }

        return result;

    }
}
