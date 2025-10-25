package com.tongji.jea.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.tongji.jea.toolWindow.JEAToolWindowFactory;
import org.jetbrains.annotations.NotNull;

public class JEAAddFileToContextAction extends AnAction {
    public JEAAddFileToContextAction() {
        super("Add this file to context");
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);
        if (project == null || file == null) return;
        // TODO:待添加上下文部分完成后添加将文件加入上下文动作
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        // 右键点击后菜单中总是显示“Add this file to context”
        e.getPresentation().setEnabledAndVisible(true);
    }
}
