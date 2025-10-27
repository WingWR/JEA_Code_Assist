package com.tongji.jea.toolWindow;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.util.ui.JBUI;
import com.tongji.jea.model.ContextItem;
import com.tongji.jea.services.ContextManagerService;
import com.tongji.jea.toolWindow.components.ContextTagComponent;
import com.tongji.jea.toolWindow.components.WrapLayout;
import com.tongji.jea.services.api.IJEACodeAssistService;
import com.tongji.jea.services.JEACodeAssistService;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class JEAToolWindowFactory implements ToolWindowFactory {
    // 保存 Project 实例供删除回调使用
    private Project project;


    // UI 组件引用（每个 ToolWindow 实例独立）
    private JPanel chatPanel;
    private JPanel tagPanel;       // 上下文标签区
    private JTextArea inputArea;

    // listener 引用，便于在 dispose 或重新创建时取消注册（这里简单实现）
    private ContextManagerService.Listener contextListener;

    /////// 创建面板方法
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        this.project = project;
        IJEACodeAssistService service = project.getService(JEACodeAssistService.class);
        ContextManagerService contextManagerService = ContextManagerService.getInstance(project);

        // 主面板，透明
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setOpaque(false);

        // 聊天消息面板
        chatPanel = createChatPanel();
        JBScrollPane chatScrollPane = new JBScrollPane(chatPanel);
        chatScrollPane.setOpaque(false);
        chatScrollPane.getViewport().setOpaque(false);
        chatScrollPane.setBorder(BorderFactory.createEmptyBorder()); // 去掉滚动边框

        // 输入区
        JPanel inputPanel = createInputPanel(service, chatPanel, chatScrollPane);

        mainPanel.add(chatScrollPane, BorderLayout.CENTER);
        mainPanel.add(inputPanel, BorderLayout.SOUTH);

        ContentFactory contentFactory = ContentFactory.getInstance();
        Content content = contentFactory.createContent(mainPanel, "", false);
        toolWindow.getContentManager().addContent(content);

        // 注册监听器：当 context 变化时刷新 tagPanel（在 UI 线程）
        contextListener = newList -> SwingUtilities.invokeLater(() -> refreshTags(newList));
        contextManagerService.addListener(contextListener);

        // 首次填充（如果 service 在别处已添加上下文）
        SwingUtilities.invokeLater(() -> refreshTags(contextManagerService.getAllContexts()));
    }


    // 聊天面板
    private JPanel createChatPanel() {
        JPanel chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        chatPanel.setOpaque(false); // 背景透明
        return chatPanel;
    }

    // 输入面板
    private JPanel createInputPanel(IJEACodeAssistService service, JPanel chatPanel, JScrollPane chatScrollPane) {
        JPanel container = new JPanel(new BorderLayout());
        container.setOpaque(false);
        container.setBackground(Gray._40);

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.X_AXIS));
        inputPanel.setOpaque(false);

        // 上下文标签区（FlowLayout，自动换行需要自行控制宽度；这里简单左对齐）
        tagPanel = new JPanel(new WrapLayout(FlowLayout.LEFT, 6, 6));
        tagPanel.setBackground(Gray._40);
        tagPanel.setOpaque(false);

        inputArea = new JTextArea(3, 40);
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        inputArea.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        inputArea.setBorder(JBUI.Borders.empty(6));

        // 只在超过最大高度时才允许滚动
        JBScrollPane inputScrollPane = new JBScrollPane(inputArea);
        inputScrollPane.setBorder(BorderFactory.createLineBorder(JBColor.GRAY));
        inputScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        int maxHeight = 120; // 最大高度，到达后再出现滚动条
        inputArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void resize() {
                inputArea.setSize(new Dimension(inputArea.getWidth(), Short.MAX_VALUE));
                int preferredHeight = inputArea.getPreferredSize().height;

                if (preferredHeight > maxHeight) {
                    inputScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
                } else {
                    inputScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
                    inputScrollPane.setPreferredSize(new Dimension(0, preferredHeight + 10));
                }
                inputPanel.revalidate();
            }

            public void insertUpdate(javax.swing.event.DocumentEvent e) { resize(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { resize(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { resize(); }
        });

        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(e -> sendMessage(service, inputArea, chatPanel, chatScrollPane));

        // ===== New Chat 按钮（放在 Send 右侧）=====
        JButton newChatButton = new JButton("New Chat");
        newChatButton.addActionListener(e -> clearConversation(service, chatPanel));

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.setOpaque(false);
        buttonPanel.add(sendButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(5, 0))); // 小间距
        buttonPanel.add(newChatButton);

        inputPanel.add(inputScrollPane);
        inputPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        inputPanel.add(buttonPanel);

        container.add(tagPanel, BorderLayout.NORTH);
        container.add(inputPanel, BorderLayout.CENTER);
        return container;
    }

    /** 根据 ContextItem 列表刷新 tagPanel（在 UI 线程调用） */
    private void refreshTags(List<ContextItem> contexts) {
        tagPanel.removeAll();
        for (ContextItem item : contexts) {
            // ContextTagComponent 是美化的标签 UI（下面会给出实现）
            ContextTagComponent tag = new ContextTagComponent(item, () -> {
                // 点击删除时，从服务移除（非 UI 直接操作数据）
                ContextManagerService.getInstance(getProjectFromComponent()).removeContext(item);
                refreshTags(contexts);
            });
            tagPanel.add(tag);
        }
        tagPanel.revalidate();
        tagPanel.repaint();
    }


    //////// 聊天面板的逻辑功能
    // 向消息窗口发送信息
    private void sendMessage(IJEACodeAssistService service, JTextArea inputArea, JPanel chatPanel, JScrollPane chatScrollPane) {
        String question = inputArea.getText().trim();
        if (question.isEmpty()) return;

        // 这里是消息的发送和接收
        addMessage(chatPanel, "You:\n" + question);

        // 显示思考提示
        JTextArea thinkingMsg = addMessage(chatPanel, "Assistant is thinking...\n");

        inputArea.setText("");
        chatPanel.revalidate();
        chatPanel.repaint();

        SwingUtilities.invokeLater(() -> chatScrollPane.getVerticalScrollBar()
                .setValue(chatScrollPane.getVerticalScrollBar().getMaximum()));

        // 在后台线程执行大模型调用
        java.util.concurrent.CompletableFuture.supplyAsync(() -> {
            return service.ask(question); // 调用模型或 API，耗时任务
        }).thenAccept(answer -> {
            // 回到主线程更新 UI
            SwingUtilities.invokeLater(() -> {
                chatPanel.remove(thinkingMsg.getParent()); // 移除“思考中”提示
                addMessage(chatPanel, "Assistant:\n" + answer);

                chatPanel.revalidate();
                chatPanel.repaint();

                JScrollBar scrollBar = chatScrollPane.getVerticalScrollBar();
                scrollBar.setValue(scrollBar.getMaximum());
            });
        }).exceptionally(ex -> {
            SwingUtilities.invokeLater(() -> {
                chatPanel.remove(thinkingMsg.getParent());
                addMessage(chatPanel, "⚠️ 出错：" + ex.getMessage());
                chatPanel.revalidate();
                chatPanel.repaint();
            });
            return null;
        });
    }

    // 在消息窗口显示信息
    private JTextArea addMessage(JPanel chatPanel, String message) {
        // 外层面板：决定左右对齐 (FlowLayout)
        JPanel messagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        messagePanel.setOpaque(false);

        // 文本区域：显示消息内容
        JTextArea messageArea = new JTextArea(message);
        messageArea.setFont(new Font("Microsoft YaHei", Font.PLAIN, 16));
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setEditable(false);
        messageArea.setOpaque(true);
        messageArea.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        // 限制宽度，让气泡不会太宽
        int maxWidth = 350;
        messageArea.setSize(new Dimension(maxWidth, Short.MAX_VALUE)); // 先设宽度
        Dimension preferredSize = messageArea.getPreferredSize();      // 再计算高度
        messageArea.setPreferredSize(new Dimension(maxWidth, preferredSize.height));

        // 添加到外层面板
        messagePanel.add(messageArea);

        // 让每条消息占一整行（BoxLayout 需要这样才不会压缩）
        messagePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, preferredSize.height + 15));

        // 最终挂到聊天窗口
        chatPanel.add(messagePanel);
        chatPanel.revalidate();
        chatPanel.repaint();

        return messageArea;
    }

    // 辅助：通过某个组件查找 Project（用于删除回调中获取 project）
    private Project getProjectFromComponent() {
        if (project == null) {
            throw new UnsupportedOperationException("请将 createToolWindowContent 中的 project 保存为实例字段以供删除回调使用。");
        }
        return project;
    }

    /**
     * 清空对话历史
     * @param service
     * @param chatPanel
     */
    private void clearConversation(IJEACodeAssistService service, JPanel chatPanel) {
        // 1. 清空前端聊天面板
        chatPanel.removeAll();
        chatPanel.revalidate();
        chatPanel.repaint();

        // 2. 清空后端对话历史
        service.clearHistory();

        // 可选：给出提示（比如加一条“新对话已开启”）
        // addMessage(chatPanel, "Assistant:\n新对话已开启。");
    }
}
