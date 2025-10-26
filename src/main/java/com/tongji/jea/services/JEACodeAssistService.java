package com.tongji.jea.services;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.tongji.jea.model.KnowledgeEntry;
import com.tongji.jea.services.api.IChatClient;
import com.tongji.jea.services.api.IContextManagerService;
import com.tongji.jea.services.api.IEmbeddingClient;
import com.tongji.jea.services.api.IRagQueryService;

import java.util.List;

/**
 * JEACodeAssistService
 * --------------------
 * 后台服务：处理与教学助手（TA）的交互逻辑。
 * 这是插件后端与前端 UI 的主要桥梁。
 */
@Service(Service.Level.PROJECT)
public final class JEACodeAssistService {

    private static final Logger LOG = Logger.getInstance(JEACodeAssistService.class);

    /** 聊天模型客户端（Qwen、OpenAI等实现） */
    private final IChatClient chatClient;

    /** 上下文管理服务（记录用户输入上下文） */
    private final IContextManagerService contextManagerService;

    /** RAG 查询服务（知识库检索） */
    private final IRagQueryService ragQueryService;


    /**
     * IntelliJ Service 构造函数
     * @param project 当前 Project
     */
    public JEACodeAssistService(Project project) {
        LOG.info("JEACodeAssistService initialized for project: " + project.getName());

        // TODO: 从插件配置中读取 API Key
        String apiKey = "sk-6ecbaca4e494438985938d406bbd5e92";

        // 初始化阿里云执行器
        DashScopeExecutor executor = new DashScopeExecutor(apiKey);

        // 初始化各模块（通过接口封装）
        this.chatClient = new ChatClient(executor, "deepseek-v3.2-exp");
        IEmbeddingClient embeddingClient = new EmbeddingHttpClient(executor, "text-embedding-v4");

        String knowledgePath = "messages/knowledge_base.json";
        this.ragQueryService = new RagQueryService(knowledgePath, (EmbeddingHttpClient) embeddingClient);
        this.contextManagerService = ContextManagerService.getInstance(project);
    }


    /**
     * 统一提问接口
     * 从上下文与知识库构造输入，调用 LLM 并返回完整回答。
     *
     * @param question 用户提问
     * @return 大模型回答 + 参考来源
     */
    public String ask(String question) {
        if (question == null || question.isBlank()) {
            return "请输入有效的问题。";
        }

        // 拼接上下文
        String contextText = contextManagerService.buildFullContextText();
        String fullQuestion = question + "\n\n" + contextText;

        // 调用 RAG 检索知识
        List<KnowledgeEntry> knowledgeEntries;
        try {
            knowledgeEntries = ragQueryService.queryKnowledgeEntries(fullQuestion, 5);
        } catch (Exception e) {
            LOG.error("RAG 检索失败：" + e.getMessage(), e);
            return "很抱歉，知识库检索失败，请稍后再试。";
        }

        // 格式化知识库上下文与来源
        String knowledgeText = ragQueryService.formatContextText(knowledgeEntries);
        String sourceSummary = ragQueryService.formatSourceSummary(knowledgeEntries);

        // 拼接完整提示输入
        String finalPrompt = fullQuestion + "\n\n" + knowledgeText;

        try {
            // 调用大模型回答
            String llmResponse = chatClient.ask(finalPrompt);
            return llmResponse + "\n\n" + sourceSummary;

        } catch (Exception e) {
            LOG.error("调用大模型失败，问题内容: " + finalPrompt, e);
            return "很抱歉，当前无法处理您的请求，请稍后再试。";
        }
    }
}
