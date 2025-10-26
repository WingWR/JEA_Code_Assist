package com.tongji.jea.services;

import com.tongji.jea.model.KnowledgeBaseLoader;
import com.tongji.jea.model.KnowledgeEntry;
import com.tongji.jea.services.api.IRagQueryService;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * RAG 查询服务类
 * 负责从知识库中检索相似内容，并生成格式化的上下文与来源摘要。
 *
 * @author qiankun25
 */
public class RagQueryService implements IRagQueryService {

    private final String knowledgeJsonPath;
    private final EmbeddingHttpClient embeddingClient;
    private List<KnowledgeEntry> kbCache = null;
    private final int topK;
    private final double confidenceThreshold;

    public RagQueryService(String knowledgeJsonPath, EmbeddingHttpClient embeddingClient,
                           int topK, double confidenceThreshold) {
        this.knowledgeJsonPath = knowledgeJsonPath;
        this.embeddingClient = embeddingClient;
        this.topK = topK;
        this.confidenceThreshold = confidenceThreshold;
    }

    /**
     * 加载知识库缓存（只加载一次）
     */
    private synchronized List<KnowledgeEntry> loadKb() {
        if (kbCache == null) {
            kbCache = KnowledgeBaseLoader.loadFromFile(knowledgeJsonPath);
        }
        return kbCache;
    }

    /**
     * 主查询函数：对输入文本进行嵌入与相似度搜索，返回高置信度的知识条目列表。
     *
     * @param inputText 用户输入或选中代码
     * @param topK 返回候选条目数量
     * @return List<KnowledgeEntry> 匹配到的知识条目
     */
    @Override
    public List<KnowledgeEntry> queryKnowledgeEntries(String inputText, int topK) {
        // 使用传入的topK参数，如果没有传入则使用配置的默认值
        int actualTopK = topK > 0 ? topK : this.topK;
        try {
            List<Double> embedding = embeddingClient.getEmbedding(inputText);
            List<KnowledgeEntry> kb = loadKb();

            List<SimilaritySearcher.ScoredEntry> results =
                    SimilaritySearcher.searchTopK(embedding, kb, topK);

            return results.stream()
                    .filter(scored -> scored.score >= this.confidenceThreshold)
                    .map(scored -> scored.entry)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException("RAG 检索失败：" + e.getMessage(), e);
        }
    }

    /**
     * 将知识条目格式化为上下文文本，供 LLM 作为提示输入。
     *
     * 格式示例：
     * 【来源】 handbook.pdf（第3页）
     * 内容：
     * ...
     *
     * 多条之间以两个换行分隔。
     */
    @Override
    public String formatContextText(List<KnowledgeEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            return "【知识库】未找到相关内容。";
        }

        StringBuilder sb = new StringBuilder();
        for (KnowledgeEntry e : entries) {
            String source = safeString(e.getSource());
            int page = e.getPage();
            String content = safeString(e.getContent());

            sb.append("\n【来源】").append(source)
                    .append("（第").append(page).append("页）\n")
                    .append("内容：\n")
                    .append(content)
                    .append("\n\n");
        }

        return sb.toString().trim();
    }

    /**
     * 将知识条目格式化为简洁的来源摘要（供 LLM 回答后附加参考来源）
     *
     * 格式示例：
     * 【参考来源】
     * - handbook.pdf（第3页）
     * - regulation.docx（第1页）
     */
    @Override
    public String formatSourceSummary(List<KnowledgeEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            return "\n【参考来源】本回答基于通用知识，未引用课程资料。";
        }

        String summary = entries.stream()
                .map(e -> "- " + e.getSource() + "（第" + e.getPage() + "页）")
                .distinct()
                .collect(Collectors.joining("\n"));

        return "【参考来源】\n" + summary;
    }

    /**
     * 异步调用封装（如果前端使用异步接口，可选）
     */
    @Override
    public CompletableFuture<List<KnowledgeEntry>> queryKnowledgeEntriesAsync(String inputText, int topK) {
        return CompletableFuture.supplyAsync(() -> queryKnowledgeEntries(inputText, topK));
    }

    /**
     * 重载：刷新知识库缓存
     */
    @Override
    public void refreshKnowledgeBase() {
        kbCache = null;
    }

    private String safeString(String s) {
        return s == null ? "" : s.trim();
    }
}
