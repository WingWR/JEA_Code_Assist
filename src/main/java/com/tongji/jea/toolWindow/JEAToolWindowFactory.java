package com.tongji.jea.toolWindow;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.util.ui.JBUI;
import com.tongji.jea.services.JEACodeAssistService;
import com.tongji.jea.toolWindow.components.ContextTagComponent;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class JEAToolWindowFactory implements ToolWindowFactory {
    /** 保存最后一个创建的聊天面板实例（供外部调用） */
    private static JEAToolWindowFactory instance;

    /** 聊天内容面板 */
    private JPanel chatPanel;
    /** 输入区文本框 */
    private JTextArea inputArea;
    /** 上下文标签区 */
    private JPanel contextPanel;
    /** 用于保存所有上下文标签内容 */
    private final List<String> contextList = new ArrayList<>();

    public JEAToolWindowFactory() {
        instance = this;
    }

    /** 获取当前实例（供 AskTAAction 使用） */
    public static JEAToolWindowFactory getInstance() {
        return instance;
    }

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        JEACodeAssistService service = project.getService(JEACodeAssistService.class);

        // 主面板，透明
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setOpaque(false);

        // ===== 上下文标签区 =====
        contextPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        contextPanel.setBackground(new Color(245, 245, 245));
        contextPanel.setBorder(BorderFactory.createTitledBorder("上下文 Context"));

        // ===== 聊天消息区 =====
        JPanel chatPanel = createChatPanel();
        latestChatPanel = chatPanel;
        JBScrollPane chatScrollPane = new JBScrollPane(chatPanel);
        chatScrollPane.getVerticalScrollBar().setUnitIncrement(16); // 设置滚动框的滚动速度
        chatScrollPane.setOpaque(false);
        chatScrollPane.getViewport().setOpaque(false);
        chatScrollPane.setBorder(BorderFactory.createEmptyBorder()); // 去掉滚动边框

        // 输入区
        JPanel inputPanel = createInputPanel(service, chatPanel, chatScrollPane);

        // 组装布局
        mainPanel.add(contextPanel, BorderLayout.NORTH);
        mainPanel.add(chatScrollPane, BorderLayout.CENTER);
        mainPanel.add(inputPanel, BorderLayout.SOUTH);

        ContentFactory contentFactory = ContentFactory.getInstance();
        Content content = contentFactory.createContent(mainPanel, "", false);
        toolWindow.getContentManager().addContent(content);
    }

    /** 创建聊天区 */
    private JPanel createChatPanel() {
        JPanel chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        chatPanel.setOpaque(false); // 背景透明
        return chatPanel;
    }

    /** 创建输入区（包含发送逻辑） */
    private JPanel createInputPanel(JEACodeAssistService service, JPanel chatPanel, JScrollPane chatScrollPane) {
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.X_AXIS));
        inputPanel.setOpaque(false);

        JTextArea inputArea = new JTextArea();
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        inputArea.setFont(new Font("Microsoft YaHei", Font.PLAIN, 16));
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

        inputPanel.add(inputScrollPane);
        inputPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        inputPanel.add(sendButton);
        return inputPanel;
    }

    /** 点击 Send 时执行 */
    private void sendMessage(JEACodeAssistService service, JTextArea inputArea, JPanel chatPanel, JScrollPane scrollPane) {
        String userInput = inputArea.getText().trim();
        if (userInput.isEmpty()) return;

        // 拼接上下文标签
        StringBuilder fullPrompt = new StringBuilder();
        if (!contextList.isEmpty()) {
            fullPrompt.append("【上下文】\n");
            for (String ctx : contextList) {
                fullPrompt.append(ctx).append("\n---\n");
            }
        }
        fullPrompt.append("【用户问题】\n").append(userInput);

        // 展示并调用服务
        addMessage(chatPanel, userInput);
        String answer = service.askTA(fullPrompt.toString());
        addMessage(chatPanel, answer);

        inputArea.setText("");
        chatPanel.revalidate();
        chatPanel.repaint();

        SwingUtilities.invokeLater(() ->
                scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum())
        );
    }

    /** 添加上下文标签（供外部调用） */
    public void addContextTag(String tagText) {
        if (contextPanel == null) return;
        ContextTagComponent tag = new ContextTagComponent(tagText, () -> removeContextTag(tagText));
        contextList.add(tagText);
        contextPanel.add(tag);
        contextPanel.revalidate();
        contextPanel.repaint();
    }

    /** 删除标签 */
    private void removeContextTag(String tagText) {
        contextList.remove(tagText);
        for (Component c : contextPanel.getComponents()) {
            if (c instanceof ContextTagComponent t && t.getTitle().equals(tagText)) {
                contextPanel.remove(t);
                break;
            }
        }
        contextPanel.revalidate();
        contextPanel.repaint();
    }

    public static void addExternalMessage(String question, String answer) {
        if (latestChatPanel == null) return;

        addMessage(latestChatPanel, "You:\n" + question);
        addMessage(latestChatPanel, "Assistant:\n" + answer);
        latestChatPanel.revalidate();
        latestChatPanel.repaint();
    }

    private static void addMessage(JPanel chatPanel, String message) {
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
    }
}
