package com.tongji.jea.toolWindow;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.tongji.jea.services.JEACodeAssistService;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class JEAToolWindowFactory implements ToolWindowFactory {
    // ===== 全局共享 UI 组件 =====
    private static JPanel sharedChatPanel = null;
    private static JScrollPane sharedScrollPane = null;
    private static DefaultListModel<String> contextListModel = new DefaultListModel<>();
    private static Map<String, String> contextFileMap = new LinkedHashMap<>();

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        JEACodeAssistService service = project.getService(JEACodeAssistService.class);

        // 主面板，透明
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setOpaque(false);

        // 聊天消息面板(更改成为与右键Action共享，以供右键选中上下文加入聊天框)
        sharedChatPanel = createChatPanel();
        sharedScrollPane = new JScrollPane(sharedChatPanel);
        sharedScrollPane.setOpaque(false);
        sharedScrollPane.getViewport().setOpaque(false);
        sharedScrollPane.setBorder(BorderFactory.createEmptyBorder()); // 去掉滚动边框

        // 输入区
        JPanel inputPanel = createInputPanel(service, sharedChatPanel, sharedScrollPane);

        // 上下文管理区
        // JPanel contextPanel = createContextPanel();

        // mainPanel.add(contextPanel, BorderLayout.NORTH);
        mainPanel.add(sharedScrollPane, BorderLayout.CENTER);
        mainPanel.add(inputPanel, BorderLayout.SOUTH);

        ContentFactory contentFactory = ContentFactory.getInstance();
        Content content = contentFactory.createContent(mainPanel, "", false);
        toolWindow.getContentManager().addContent(content);
    }

    // 创建上下文管理区，待处理完善
    /*private JPanel createContextPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Context Manager"));

        // 文件列表
        JList<String> contextList = new JList<>(contextListModel);
        JScrollPane scrollPane = new JScrollPane(contextList);

        // 清空按钮
        JButton clearButton = new JButton("Clear Context");
        clearButton.addActionListener(e -> {
            contextListModel.clear();
            contextFileMap.clear();
            appendSystemMessage("Context cleared.");
        });

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(clearButton, BorderLayout.SOUTH);
        return panel;
    }*/

    private JPanel createChatPanel() {
        JPanel chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        chatPanel.setOpaque(false); // 背景透明
        return chatPanel;
    }

    private JPanel createInputPanel(JEACodeAssistService service, JPanel chatPanel, JScrollPane chatScrollPane) {
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.X_AXIS));
        inputPanel.setOpaque(false);

        JTextArea inputArea = new JTextArea();
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        inputArea.setFont(new Font("Microsoft YaHei", Font.PLAIN, 16));
        inputArea.setMargin(new Insets(6, 6, 6, 6));

        // 只在超过最大高度时才允许滚动
        JScrollPane inputScrollPane = new JScrollPane(inputArea);
        inputScrollPane.setBorder(BorderFactory.createLineBorder(Color.GRAY));
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

        inputPanel.add(inputScrollPane);
        inputPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        inputPanel.add(sendButton);
        return inputPanel;
    }

    private void sendMessage(JEACodeAssistService service, JTextArea inputArea, JPanel chatPanel, JScrollPane chatScrollPane) {
        String question = inputArea.getText().trim();
        if (question.isEmpty()) return;

        addMessage(chatPanel, question, true, false);
        String answer = service.askTA(question);
        addMessage(chatPanel, answer, false, false);

        inputArea.setText("");
        chatPanel.revalidate();
        chatPanel.repaint();

        SwingUtilities.invokeLater(() -> chatScrollPane.getVerticalScrollBar()
                .setValue(chatScrollPane.getVerticalScrollBar().getMaximum()));
    }

    private static void addMessage(JPanel chatPanel, String message, boolean isUser, boolean isCode) {
        // 外层面板：决定左右对齐 (FlowLayout)
        JPanel messagePanel = new JPanel(new FlowLayout(isUser ? FlowLayout.RIGHT : FlowLayout.LEFT));
        messagePanel.setOpaque(false);

        // 文本区域：显示消息内容
        JTextArea messageArea = new JTextArea(message);
        messageArea.setFont(new Font("Microsoft YaHei", Font.PLAIN, 16));
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setEditable(false);
        messageArea.setOpaque(true);
        messageArea.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        // 右侧消息文字靠右显示
        if (isUser) {
            messageArea.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        }

        // 如果是上下文代码显示背景为灰色
        if (isCode) {
            messageArea.setBackground(new Color(245, 245, 245));
            messageArea.setForeground(new Color(60, 60, 60));
        }

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
    }

    // 外部接口，供右键选择上下文加入聊天框
    public static void appendCodeContext(String code) {
        if (sharedChatPanel == null) return;
        SwingUtilities.invokeLater(() -> {
            addMessage(sharedChatPanel, "Selected code:", true, false);
            addMessage(sharedChatPanel, code, true, true);
            scrollToBottom();
        });
    }

    private static void scrollToBottom() {
        if (sharedScrollPane != null) {
            JScrollBar bar = sharedScrollPane.getVerticalScrollBar();
            bar.setValue(bar.getMaximum());
        }
    }
}
