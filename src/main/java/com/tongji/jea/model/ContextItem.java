package com.tongji.jea.model;

public class ContextItem {
    private final String label;
    private final String type; // e.g. "file", "code", "note"
    private final String content;

    public ContextItem(String label, String type, String content) {
        this.label = label;
        this.type = type;
        this.content = content;
    }

    public String getLabel() { return label; }
    public String getType() { return type; }
    public String getContent() { return content; }

    @Override
    public String toString() {
        return "[" + type + "] " + label;
    }
}
