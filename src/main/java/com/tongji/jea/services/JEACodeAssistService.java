package com.tongji.jea.services;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.tongji.jea.model.KnowledgeEntry;
import com.tongji.jea.services.ChatClient;
import com.tongji.jea.services.DashScopeExecutor;
import com.tongji.jea.services.RagQueryService;
import com.tongji.jea.model.ChatMessage;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service(Service.Level.PROJECT)
/**
 * 后台服务，处理与教学助手（TA）的交互
 * 即后端与前端的交互点
 */
public final class JEACodeAssistService {
    private static final Logger LOG = Logger.getInstance(JEACodeAssistService.class);
    private final ChatClient chatClient;
    private final ContextManagerService contextManagerService;
    private final RagQueryService ragQueryService;
    private final List<ChatMessage> conversationHistory = new ArrayList<>();
    private static final String SYSTEM_PROMPT =
            "你是由同济大学开发的 IntelliJ IDEA 插件中的教学助教，专门服务于《Java 企业应用开发》课程。" +
                    "你的核心职责是回答与该课程内容相关的编程问题，并严格遵循以下规则：" +
                    "1. 所有回答：语言简洁、专业、准确。" +
                    "2. 禁止使用 Markdown 语法（如 **加粗**、```代码块```、标题等），所有代码示例必须以纯文本形式内联或分行写出" +
                    "3. 回答应结合用户当前代码上下文（如选中的代码段），提供有针对性的解释或建议。"
                    ;

    public JEACodeAssistService(Project project) {
        LOG.info("JEACodeAssistService initialized for project: " + project.getName());
        String apiKey = "sk-6ecbaca4e494438985938d406bbd5e92";//TODO:统一从配置读取
        DashScopeExecutor executor = new DashScopeExecutor(apiKey);

        this.chatClient = new ChatClient(executor, "qwen3-max-2025-09-23");//模型可替换，qwen-max etc.

        String knowledgePath = "messages/knowledge_base.json";//resourcePath 路径下的相对路径
        EmbeddingHttpClient embeddingHttpClient = new EmbeddingHttpClient(executor, "text-embedding-v4");
        this.ragQueryService = new RagQueryService(knowledgePath, embeddingHttpClient);
        this.contextManagerService = ContextManagerService.getInstance(project);
    }


    public String ask(String question) {
        // TODO: 待添加获取LLM的回复逻辑
        //添加上下文内容
        question += contextManagerService.buildFullContextText();
        //添加知识库逻辑
        List<KnowledgeEntry> knowledgeEntries = ragQueryService.queryKnowledgeEntries(question,5);
        //格式化提问知识库内容
        String knowledgeText = ragQueryService.formatContextText(knowledgeEntries);
        //格式化知识库来源条目
        String sourceSummary  = ragQueryService.formatSourceSummary(knowledgeEntries);

        //拼接问题上下文
        question += knowledgeText;

        conversationHistory.add(new ChatMessage("user", question));

        //设置系统提示词
        // 构造完整请求历史：system + conversationHistory
        List<ChatMessage> fullHistory = new ArrayList<>();
        fullHistory.add(new ChatMessage("system", SYSTEM_PROMPT)); // 每次都加！
        fullHistory.addAll(conversationHistory);


        String finalResponse;

        try{
            String llmResponse = chatClient.ask(fullHistory);
            //拼接大模型响应+知识库条目
            finalResponse = llmResponse + sourceSummary;
        }
        catch (Exception e) {
            finalResponse = "很抱歉，当前无法处理您的请求，请稍后再试。";
            // 记录详细错误日志，包含上下文
            LOG.error("调用大模型失败，问题内容: {}"+question, e);
        }
        //将模型回复加入历史（用于下一轮上下文）
        conversationHistory.add(new ChatMessage("assistant", finalResponse));

        return finalResponse;
    }

    /**
     * 清空对话历史（供前端“新建对话”等操作调用）
     */
    public void clearHistory() {
        conversationHistory.clear();
    }

    /**
     * 获取当前对话历史（用于调试或前端显示）
     */
    public List<ChatMessage> getHistory() {
        return new ArrayList<>(conversationHistory); // 返回副本避免外部修改
    }
}
