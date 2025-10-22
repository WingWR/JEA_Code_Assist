package com.github.wingwr.javacodeassist.services;

import com.github.wingwr.javacodeassist.data.KnowledgeEntry;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 余弦相似度搜索器（在 Kotlin 内部执行）
 */
public class SimilaritySearcher {
    public static double cosineSimilarity(List<Double> a, List<Double> b) {
        int n = Math.min(a.size(), b.size());
        double dot = 0.0, na = 0.0, nb = 0.0;
        for (int i = 0; i < n; i++) {
            double ai = a.get(i);
            double bi = b.get(i);
            dot += ai * bi;
            na += ai * ai;
            nb += bi * bi;
        }
        if (na == 0.0 || nb == 0.0) return 0.0;
        return dot / (Math.sqrt(na) * Math.sqrt(nb));
    }


    /**
     * 在本地知识库中查找 topK 最相似的条目
     * @param queryEmbedding
     * @param kb
     * @param topK
     * @return 返回相似度和向量条目对象列表
     */
    public static List<ScoredEntry> searchTopK(List<Double> queryEmbedding,
                                               List<KnowledgeEntry> kb,
                                               int topK) {
        return kb.stream()
                .map(entry -> new ScoredEntry(cosineSimilarity(queryEmbedding, entry.getEmbedding()), entry))
                .sorted((a, b) -> Double.compare(b.score, a.score))
                .limit(topK)
                .collect(Collectors.toList());
    }

    public static class ScoredEntry {
        public final double score;
        public final KnowledgeEntry entry;

        public ScoredEntry(double score, KnowledgeEntry entry) {
            this.score = score;
            this.entry = entry;
        }
    }
}
