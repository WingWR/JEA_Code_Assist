package com.tongji.jea.actions;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.PsiFile;
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

        String fullContext = getString(editor, psiFile);

        // 打开 ToolWindow
        ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow("JEA Assistant");
        if (toolWindow != null) toolWindow.activate(null);

        // 添加上下文标签
        JEAToolWindowFactory factory = JEAToolWindowFactory.getInstance();
        if (factory != null) {
            factory.addContextTag(fullContext);
        }
    }

    private static @NotNull String getString(Editor editor, PsiFile psiFile) {
        String selectedText = editor.getSelectionModel().getSelectedText();
        if (selectedText == null || selectedText.isBlank()) {
            selectedText = "(未选择代码)";
        }

        Document doc = editor.getDocument();
        int startLine = doc.getLineNumber(editor.getSelectionModel().getSelectionStart()) + 1;
        int endLine = doc.getLineNumber(editor.getSelectionModel().getSelectionEnd()) + 1;
        String fileName = psiFile.getVirtualFile().getName();

        // 标签显示文字（简洁）
        String contextSummary = String.format("%s [%d-%d]", fileName, startLine, endLine);

        // 标签详细内容（供发送时使用）
        String fullContext = String.format("文件名: %s\n选中行: %d - %d\n代码:\n%s", fileName, startLine, endLine, selectedText);
        return fullContext;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        e.getPresentation().setEnabledAndVisible(editor != null);
    }
}
