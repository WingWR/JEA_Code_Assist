package com.tongji.jea.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.openapi.diagnostic.Logger;

import java.io.InputStream;

/**
 * 插件配置管理器
 * 负责从资源文件加载插件配置
 */
public class PluginConfigManager {
    private static final Logger LOG = Logger.getInstance(PluginConfigManager.class);

    private static final String CONFIG_FILE_PATH = "config/pluginConfig.json";
    private static PluginConfig instance;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 获取配置实例（单例模式）
     */
    public static synchronized PluginConfig getConfig() {
        if (instance == null) {
            instance = loadConfig();
        }
        return instance;
    }

    /**
     * 重新加载配置
     */
    public static synchronized void reloadConfig() {
        instance = loadConfig();
    }

    /**
     * 从资源文件加载配置
     */
    private static PluginConfig loadConfig() {
        try {
            LOG.info("从资源文件加载配置: " + CONFIG_FILE_PATH);
            InputStream inputStream = PluginConfigManager.class.getClassLoader()
                    .getResourceAsStream(CONFIG_FILE_PATH);

            if (inputStream != null) {
                return objectMapper.readValue(inputStream, PluginConfig.class);
            } else {
                LOG.warn("资源文件中未找到配置文件，使用默认配置");
                return createDefaultConfig();
            }

        } catch (Exception e) {
            LOG.error("加载配置文件失败，使用默认配置", e);
            return createDefaultConfig();
        }
    }

    /**
     * 创建默认配置（备用）
     */
    private static PluginConfig createDefaultConfig() {
        return new PluginConfig(
                "sk-6ecbaca4e494438985938d406bbd5e92",
                "https://dashscope.aliyuncs.com",
                "deepseek-v3.2-exp",
                "text-embedding-v4",
                "messages/knowledge_base.json",
                5,
                0.70,
                20,
                60,
                "你是由同济大学开发的 IntelliJ IDEA 插件中的教学助教，专门服务于《Java 企业应用开发》课程。" +
                        "你的核心职责是回答与该课程内容相关的编程问题，并严格遵循以下规则：" +
                        "1. 所有回答：语言简洁、专业、准确。" +
                        "2. 禁止使用 Markdown 语法（如 **加粗**、```代码块```、标题等），所有代码示例必须以纯文本形式内联或分行写出" +
                        "3. 回答应结合用户当前代码上下文（如选中的代码段），提供有针对性的解释或建议。"
        );
    }
}