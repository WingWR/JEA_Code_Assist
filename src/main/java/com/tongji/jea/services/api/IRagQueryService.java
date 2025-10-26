package com.tongji.jea.services.api;

import com.tongji.jea.model.KnowledgeEntry;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * RAG 知识检索服务接口。
 */
public interface IRagQueryService {
    List<KnowledgeEntry> queryKnowledgeEntries(String inputText, int topK);

    String formatContextText(List<KnowledgeEntry> entries);

    String formatSourceSummary(List<KnowledgeEntry> entries);

    CompletableFuture<List<KnowledgeEntry>> queryKnowledgeEntriesAsync(String inputText, int topK);

    void refreshKnowledgeBase();
}
