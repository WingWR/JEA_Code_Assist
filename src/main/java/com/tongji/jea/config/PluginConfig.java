package com.tongji.jea.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;

public class PluginConfig {

    public LLMConfig llm = new LLMConfig();
    public RagConfig rag = new RagConfig();
    public ContextConfig context = new ContextConfig();
    public LoggingConfig logging = new LoggingConfig();

    public static class LLMConfig {
        public String provider = "aliyun";
        public String base_url = "https://dashscope.aliyuncs.com/api/v1";
        public String api_key = "";
        public String model = "qwen-turbo";
        public double temperature = 0.7;
        public int max_tokens = 2048;
    }

    public static class RagConfig {
        public boolean enabled = true;
        public String knowledge_path = "messages/knowledge_base.json";
        public String embedding_model = "text-embedding-v4";
        public int max_results = 5;
    }

    public static class ContextConfig {
        public boolean enabled = true;
        public int max_history = 10;
    }

    public static class LoggingConfig {
        public String level = "INFO";
    }

    private static PluginConfig instance;

    public static PluginConfig getInstance() {
        if (instance == null) {
            instance = loadConfig();
        }
        return instance;
    }

    private static PluginConfig loadConfig() {
        try (InputStream is = PluginConfig.class.getClassLoader()
                .getResourceAsStream("config/pluginConfig.json")) {
            if (is == null) {
                throw new RuntimeException("pluginConfig.json not found in resources/config/");
            }
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(is, PluginConfig.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load pluginConfig.json", e);
        }
    }
}
