package com.tongji.jea.actions;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.PsiFile;
import com.tongji.jea.model.ContextItem;
import com.tongji.jea.services.ContextManagerService;
import com.tongji.jea.toolWindow.JEAToolWindowFactory;
import org.jetbrains.annotations.NotNull;

public class AskTAAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;

        Editor editor = e.getData(CommonDataKeys.EDITOR);
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        if (editor == null || psiFile == null) return;

        String selectedText = editor.getSelectionModel().getSelectedText();
        if (selectedText == null || selectedText.isBlank()) {
            selectedText = "未选择上下文";
        }

        Document doc = editor.getDocument();
        int startLine = doc.getLineNumber(editor.getSelectionModel().getSelectionStart()) + 1;
        int endLine = doc.getLineNumber(editor.getSelectionModel().getSelectionEnd()) + 1;
        String fileName = psiFile.getVirtualFile().getName();

        ContextItem contextItem = new ContextItem(fileName + " [" + startLine + "-" + endLine + "]", "code", selectedText);

        ContextManagerService.getInstance(project).addContext(contextItem);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        e.getPresentation().setEnabledAndVisible(editor != null);
    }
}
