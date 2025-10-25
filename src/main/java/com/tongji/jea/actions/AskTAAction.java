package com.tongji.jea.actions;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.PsiFile;
import com.tongji.jea.services.JEACodeAssistService;
import com.tongji.jea.toolWindow.JEAToolWindowFactory;
import groovyjarjarantlr4.v4.runtime.misc.NotNull;

import javax.swing.*;

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
            selectedText = "(未选择代码)";
        }

        // 获取文件名、选中行号
        Document document = editor.getDocument();
        int startLine = document.getLineNumber(editor.getSelectionModel().getSelectionStart()) + 1;
        int endLine = document.getLineNumber(editor.getSelectionModel().getSelectionEnd()) + 1;
        String fileName = psiFile.getVirtualFile().getName();

        // 拼接上下文
        StringBuilder questionBuilder = new StringBuilder();
        questionBuilder.append("文件名：").append(fileName)
                .append("\n选中行：").append(startLine).append(" - ").append(endLine)
                .append("\n\n代码内容：\n").append(selectedText)
                .append("\n\n请帮我分析这段代码的作用、潜在问题和改进建议。");

        String question = questionBuilder.toString();

        // 调用 JEACodeAssistService
        JEACodeAssistService jeaCodeAssistService = project.getService(JEACodeAssistService.class);
        String response = jeaCodeAssistService.ask(question);

        //  打开右侧 ToolWindow（即 JEAToolWindowFactory 创建的）
        ToolWindow toolWindow = ToolWindowManager.getInstance(project)
                .getToolWindow("JEA Assistant");
        if (toolWindow != null) {
            toolWindow.activate(null);
        }

        //  向聊天面板添加内容
        SwingUtilities.invokeLater(() -> {
            JEAToolWindowFactory.addExternalMessage(question, response);
        });
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        e.getPresentation().setEnabledAndVisible(editor != null);
    }
}
