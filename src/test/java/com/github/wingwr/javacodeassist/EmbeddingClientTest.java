package com.github.wingwr.javacodeassist;

import com.github.wingwr.javacodeassist.services.EmbeddingHttpClient;
import com.github.wingwr.javacodeassist.services.DashScopeExecutor;

public class EmbeddingClientTest {
    public static void main(String[] args) {
        String apiKey = "sk-6ecbaca4e494438985938d406bbd5e92";
        DashScopeExecutor executor = new DashScopeExecutor(apiKey);
        EmbeddingHttpClient embeddingClient = new EmbeddingHttpClient(executor, "text-embedding-v4");
        try {
            double[] vector = embeddingClient.getEmbedding("Java plugin development in IntelliJ.");
            System.out.println("Embedding length: " + vector.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}