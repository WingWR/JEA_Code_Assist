package com.tongji.jea.actions;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.tongji.jea.model.ContextItem;
import com.tongji.jea.services.ContextManagerService;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;

public class AddToContextAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);

        if (project == null || file == null) return;

        try {
            String content = new String(file.contentsToByteArray(), StandardCharsets.UTF_8);

            ContextItem item = new ContextItem(file.getName(), "file", content);
            ContextManagerService.getInstance(project).addContext(item);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabledAndVisible(e.getData(CommonDataKeys.VIRTUAL_FILE) != null);
    }
}
