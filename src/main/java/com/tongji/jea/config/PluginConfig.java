package com.tongji.jea.config;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 插件配置类
 * 用于管理所有可配置参数
 */
public class PluginConfig {

    // API 配置
    @JsonProperty("apiKey")
    private String apiKey;

    @JsonProperty("baseUrl")
    private String baseUrl;

    // 模型配置
    @JsonProperty("chatModel")
    private String chatModel;

    @JsonProperty("embeddingModel")
    private String embeddingModel;

    // 路径配置
    @JsonProperty("knowledgeBasePath")
    private String knowledgeBasePath;

    // RAG 配置
    @JsonProperty("topK")
    private int topK;

    @JsonProperty("confidenceThreshold")
    private double confidenceThreshold;

    // HTTP 配置
    @JsonProperty("connectTimeoutSeconds")
    private int connectTimeoutSeconds;

    @JsonProperty("readTimeoutSeconds")
    private int readTimeoutSeconds;

    // 系统提示词
    @JsonProperty("systemPrompt")
    private String systemPrompt;

    // 默认构造函数（Jackson 需要）
    public PluginConfig() {}

    // 带参构造函数
    public PluginConfig(String apiKey, String baseUrl, String chatModel, String embeddingModel,
                        String knowledgeBasePath, int topK, double confidenceThreshold,
                        int connectTimeoutSeconds, int readTimeoutSeconds, String systemPrompt) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.chatModel = chatModel;
        this.embeddingModel = embeddingModel;
        this.knowledgeBasePath = knowledgeBasePath;
        this.topK = topK;
        this.confidenceThreshold = confidenceThreshold;
        this.connectTimeoutSeconds = connectTimeoutSeconds;
        this.readTimeoutSeconds = readTimeoutSeconds;
        this.systemPrompt = systemPrompt;
    }

    // Getter 和 Setter 方法
    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getChatModel() {
        return chatModel;
    }

    public void setChatModel(String chatModel) {
        this.chatModel = chatModel;
    }

    public String getEmbeddingModel() {
        return embeddingModel;
    }

    public void setEmbeddingModel(String embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    public String getKnowledgeBasePath() {
        return knowledgeBasePath;
    }

    public void setKnowledgeBasePath(String knowledgeBasePath) {
        this.knowledgeBasePath = knowledgeBasePath;
    }

    public int getTopK() {
        return topK;
    }

    public void setTopK(int topK) {
        this.topK = topK;
    }

    public double getConfidenceThreshold() {
        return confidenceThreshold;
    }

    public void setConfidenceThreshold(double confidenceThreshold) {
        this.confidenceThreshold = confidenceThreshold;
    }

    public int getConnectTimeoutSeconds() {
        return connectTimeoutSeconds;
    }

    public void setConnectTimeoutSeconds(int connectTimeoutSeconds) {
        this.connectTimeoutSeconds = connectTimeoutSeconds;
    }

    public int getReadTimeoutSeconds() {
        return readTimeoutSeconds;
    }

    public void setReadTimeoutSeconds(int readTimeoutSeconds) {
        this.readTimeoutSeconds = readTimeoutSeconds;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    @Override
    public String toString() {
        return "PluginConfig{" +
                "apiKey='" + (apiKey != null ? "***" : "null") + '\'' +
                ", baseUrl='" + baseUrl + '\'' +
                ", chatModel='" + chatModel + '\'' +
                ", embeddingModel='" + embeddingModel + '\'' +
                ", knowledgeBasePath='" + knowledgeBasePath + '\'' +
                ", topK=" + topK +
                ", confidenceThreshold=" + confidenceThreshold +
                ", connectTimeoutSeconds=" + connectTimeoutSeconds +
                ", readTimeoutSeconds=" + readTimeoutSeconds +
                ", systemPromptLength=" + (systemPrompt != null ? systemPrompt.length() : 0) +
                '}';
    }
}