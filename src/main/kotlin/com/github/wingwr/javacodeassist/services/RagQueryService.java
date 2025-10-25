package com.github.wingwr.javacodeassist.services;

import com.github.wingwr.javacodeassist.data.KnowledgeBaseLoader;
import com.github.wingwr.javacodeassist.data.KnowledgeEntry;

import java.util.List;
import java.util.concurrent.CompletableFuture;
public class RagQueryService {

    private final String knowledgeJsonPath;
    private final EmbeddingHttpClient embeddingClient;
    private List<KnowledgeEntry> kbCache = null;

    public RagQueryService(String knowledgeJsonPath, EmbeddingHttpClient embeddingClient) {
        this.knowledgeJsonPath = knowledgeJsonPath;
        this.embeddingClient = embeddingClient;
    }

    /**
     *
     * @return 知识条目列表缓存
     * @author qiankun25
     */
    private synchronized List<KnowledgeEntry> loadKb() {
        if (kbCache == null) {
            kbCache = KnowledgeBaseLoader.loadFromFile(knowledgeJsonPath);
        }
        return kbCache;
    }

    /**
     *
     * @param selectedCode
     * @param topK
     * @return  异步返回一个包含相似条目和相似度的列表
     */
    public CompletableFuture<List<SimilaritySearcher.ScoredEntry>> querySelectedCodeAndSearch(String selectedCode, int topK) {

        // 1. 同步获取文本向量
        // 2. 异步执行相似度搜索
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<Double> embedding = embeddingClient.getEmbedding(selectedCode);
                List<KnowledgeEntry> kb = loadKb();
                return SimilaritySearcher.searchTopK(embedding, kb, topK);
            } catch (Exception e) {
                throw new RuntimeException("Embedding failed", e);
            }
        });
    }

    public void refreshKnowledgeBase() {
        kbCache = null;
    }
}
