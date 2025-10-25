package com.tongji.jea.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.tongji.jea.services.JEACodeAssistService;
import com.tongji.jea.toolWindow.JEAToolWindowFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JEAAskFromCodeAction extends AnAction {
    public JEAAskFromCodeAction(){
        super("Ask JEA about this code");
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (project == null || editor == null) return;

        // 安全读取选中代码
        ApplicationManager.getApplication().runReadAction(() -> {
            String selectedCode = editor.getSelectionModel().getSelectedText();
            if (selectedCode == null || selectedCode.isEmpty()) return;

            // 回到 UI 线程更新聊天界面
            ApplicationManager.getApplication().invokeLater(() -> JEAToolWindowFactory.appendCodeContext(selectedCode));
        });
    }


    @Override
    public void update(@NotNull AnActionEvent e) {
        // 只有选中代码时才在右键菜单中显示“Ask JEA about this code”
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        boolean hasSelection = editor != null && editor.getSelectionModel().hasSelection();
        e.getPresentation().setEnabledAndVisible(hasSelection);
    }
}
