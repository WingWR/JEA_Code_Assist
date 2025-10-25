package com.tongji.jea.model;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 从 classpath 中加载 JSON 资源文件到 List<KnowledgeEntry>
 *
 * @param resourcePath classpath 下的相对路径，例如 "messages/knowledge_base.json"
 * @return 解析后的知识条目列表
 * @throws IllegalArgumentException 如果资源未找到
 * @throws RuntimeException         如果读取或解析失败
 * @author qiankun25
 */
public class KnowledgeBaseLoader {
    public static List<KnowledgeEntry> loadFromFile(String resourcePath) {
        try (InputStream is = KnowledgeEntry.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IllegalArgumentException("Knowledge base resource not found in classpath: " + resourcePath);
            }

            StringBuilder content = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append('\n');
                }
            }

            Gson gson = new Gson();
            Type listType = new TypeToken<List<KnowledgeEntry>>() {
            }.getType();
            return gson.fromJson(content.toString(), listType);

        } catch (IOException e) {
            throw new RuntimeException("Failed to read knowledge base resource: " + resourcePath, e);
        }
    }
}