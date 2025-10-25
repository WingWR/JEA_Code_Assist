package com.tongji.jea.model;

import java.util.List;

public class KnowledgeEntry {
    private String text;
    private List<Double> embedding;
    private String source;
    private int page;
    private String content;

    // Getters and Setters
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public List<Double> getEmbedding() { return embedding; }
    public void setEmbedding(List<Double> embedding) { this.embedding = embedding; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
