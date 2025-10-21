package com.tongji.jea;

import org.jetbrains.annotations.NotNull;

import java.util.ResourceBundle;

public class MyBundle {
    private static final String BUNDLE = "messages.MyBundle"; // 对应 resources/messages/MyBundle.properties
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE);

    private MyBundle() { }

    @NotNull
    public static String message(@NotNull String key) {
        return RESOURCE_BUNDLE.getString(key);
    }
}
