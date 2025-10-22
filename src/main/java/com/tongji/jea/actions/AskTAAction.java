package com.tongji.jea.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.tongji.jea.services.LLMService;
import org.jetbrains.annotations.NotNull;

public class AskTAAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        Editor editor = e.getData(CommonDataKeys.EDITOR);

        if (editor != null) {
            SelectionModel selectionModel = editor.getSelectionModel();
            String selectedText = selectionModel.getSelectedText();

            if (selectedText != null && !selectedText.isEmpty()) {
                LLMService llm = new LLMService();
                String response = llm.ask("Explain this code:\n" + selectedText);
                System.out.println("TA Response:\n" + response);
            } else {
                System.out.println("No code selected.");
            }
        }
    }
}
