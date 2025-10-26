package com.tongji.jea.services;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.tongji.jea.model.KnowledgeEntry;
import com.tongji.jea.services.api.*;
import com.tongji.jea.model.ChatMessage;
import com.tongji.jea.config.PluginConfig;
import com.tongji.jea.config.PluginConfigManager;

import java.util.ArrayList;
import java.util.List;


/**
 * 后台服务，处理与教学助手（TA）的交互
 * 即后端与前端的交互点
 */
@Service(Service.Level.PROJECT)
public final class JEACodeAssistService implements IJEACodeAssistService {
    private static final Logger LOG = Logger.getInstance(JEACodeAssistService.class);
    /**
     * 聊天模型客户端（Qwen、OpenAI等实现）
     */
    private final IChatClient chatClient;

    /**
     * 上下文管理服务（记录用户输入上下文）
     */
    private final IContextManagerService contextManagerService;

    /**
     * RAG 查询服务（知识库检索）
     */
    private final IRagQueryService ragQueryService;
    private final List<ChatMessage> conversationHistory = new ArrayList<>();

    /**
     * 系统提示词
     */
    private final String systemPrompt;


    public JEACodeAssistService(Project project) {
        LOG.info("JEACodeAssistService initialized for project: " + project.getName());
        // 从配置管理器获取配置
        PluginConfig config = PluginConfigManager.getConfig();
        this.systemPrompt = config.getSystemPrompt();

        // 使用配置参数初始化执行器
        DashScopeExecutor executor = new DashScopeExecutor(
                config.getApiKey(),
                config.getBaseUrl(),
                config.getConnectTimeoutSeconds(),
                config.getReadTimeoutSeconds()
        );

        // 初始化各模块
        this.chatClient = new ChatClient(executor, config.getChatModel());
        IEmbeddingClient embeddingClient = new EmbeddingHttpClient(executor, config.getEmbeddingModel());

        // 使用配置的知识库路径和参数初始化RAG服务
        this.ragQueryService = new RagQueryService(
                config.getKnowledgeBasePath(),
                (EmbeddingHttpClient) embeddingClient,
                config.getTopK(),
                config.getConfidenceThreshold()
        );

        this.contextManagerService = ContextManagerService.getInstance(project);
        LOG.info("JEACodeAssistService 初始化完成，使用模型: " + config.getChatModel());

    }


    /**
     * 统一提问接口
     * 从上下文与知识库构造输入，调用 LLM 并返回完整回答。
     *
     * @param question 用户提问
     * @return 大模型回答 + 参考来源
     */
    @Override
    public String ask(String question) {
        if (question == null || question.trim().isEmpty()) {
            return "问题不能为空，请输入有效的问题。";
        }

        String finalQuestion = buildEnhancedQuestion(question);

        // 构造完整对话历史
        List<ChatMessage> fullHistory = buildFullHistory(finalQuestion);

        String finalResponse = processLLMRequest(fullHistory, finalQuestion);

        // 更新对话历史
        updateConversationHistory(question, finalResponse);

        return finalResponse;
    }

    /**
     * 添加上下文和知识库内容
     */
    private String buildEnhancedQuestion(String originalQuestion) {
        StringBuilder enhancedQuestion = new StringBuilder(originalQuestion);

        // 添加上下文内容
        String contextText = contextManagerService.buildFullContextText();
        if (!contextText.isEmpty()) {
            enhancedQuestion.append("\n\n【当前代码上下文】\n").append(contextText);
        }

        // 添加知识库内容
        List<KnowledgeEntry> knowledgeEntries = ragQueryService.queryKnowledgeEntries(originalQuestion, 5);
        String knowledgeText = ragQueryService.formatContextText(knowledgeEntries);
        if (!knowledgeText.contains("未找到相关内容")) {
            enhancedQuestion.append("\n\n【相关课程资料】\n").append(knowledgeText);
        }

        return enhancedQuestion.toString();
    }

    /**
     * 构建完整的对话历史
     */
    private List<ChatMessage> buildFullHistory(String finalQuestion) {
        List<ChatMessage> fullHistory = new ArrayList<>();

        // 添加系统提示词
        fullHistory.add(new ChatMessage("system", systemPrompt));

        // 添加历史对话
        fullHistory.addAll(conversationHistory);

        // 添加当前问题
        fullHistory.add(new ChatMessage("user", finalQuestion));

        return fullHistory;
    }

    /**
     * 处理LLM请求
     */
    private String processLLMRequest(List<ChatMessage> fullHistory, String question) {
        try {
            String llmResponse = chatClient.ask(fullHistory);

            // 添加知识库来源摘要
            List<KnowledgeEntry> knowledgeEntries = ragQueryService.queryKnowledgeEntries(question, 5);
            String sourceSummary = ragQueryService.formatSourceSummary(knowledgeEntries);

            return llmResponse + sourceSummary;

        } catch (Exception e) {
            LOG.error("调用大模型失败，问题内容: " + question, e);
            return "很抱歉，当前无法处理您的请求，请稍后再试。错误详情: " + e.getMessage();
        }
    }

    /**
     * 更新对话历史
     */
    private void updateConversationHistory(String originalQuestion, String response) {
        // 保存原始问题和完整响应
        conversationHistory.add(new ChatMessage("user", originalQuestion));
        conversationHistory.add(new ChatMessage("assistant", response));

        // 可选：限制历史记录长度，避免内存溢出
        if (conversationHistory.size() > 20) { // 保留最近10轮对话
            conversationHistory.subList(0, 4).clear(); // 移除前2轮对话
        }
    }

    /**
     * 清空对话历史（供前端“新建对话”等操作调用）
     */
    @Override
    public void clearHistory() {
        conversationHistory.clear();
    }

    /**
     * 获取当前对话历史（用于调试或前端显示）
     */
    @Override
    public List<ChatMessage> getHistory() {
        return new ArrayList<>(conversationHistory); // 返回副本避免外部修改
    }


}
