package com.tongji.jea.listeners;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 项目启动时的活动监听器
 */
public class JEACodeAssistActivityListener implements ProjectActivity {
    private static final Logger LOG = Logger.getInstance(JEACodeAssistActivityListener.class);

    @Override
    public @Nullable Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
        LOG.info("JEACodeAssistActivityListener loaded successfully.");
        return Unit.INSTANCE;
    }
}
