package com.tongji.jea;

import com.tongji.jea.services.DashScopeExecutor;
import com.tongji.jea.services.EmbeddingHttpClient;

import java.util.List;

public class EmbeddingClientTest {
    public static void main(String[] args) {
        String apiKey = "sk-6ecbaca4e494438985938d406bbd5e92";
        DashScopeExecutor executor = new DashScopeExecutor(apiKey);
        EmbeddingHttpClient embeddingClient = new EmbeddingHttpClient(executor, "text-embedding-v4");
        try {
            List<Double> vector = embeddingClient.getEmbedding("Java plugin development in IntelliJ.");
            System.out.println("Embedding length: " + vector.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}