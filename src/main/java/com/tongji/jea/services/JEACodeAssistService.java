package com.tongji.jea.services;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.tongji.jea.model.KnowledgeEntry;
import com.tongji.jea.services.ChatClient;
import com.tongji.jea.services.DashScopeExecutor;
import com.tongji.jea.services.RagQueryService;


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
    private final RagQueryService ragQueryService;

    public JEACodeAssistService(Project project) {
        LOG.info("JEACodeAssistService initialized for project: " + project.getName());
        String apiKey = "sk-7d351140e99c4744a37d73b67bfe7592";//TODO:统一从配置读取
        DashScopeExecutor executor = new DashScopeExecutor(apiKey);

        this.chatClient = new ChatClient(executor, "qwen-turbo");//模型可替换，qwen-max etc.

        String knowledgePath = "messages/knowledge_base.json";//resourcePath 路径下的相对路径
        EmbeddingHttpClient embeddingHttpClient = new EmbeddingHttpClient(executor, "text-embedding-v4");
        this.ragQueryService = new RagQueryService(knowledgePath, embeddingHttpClient);
    }


    public String ask(String question) {
        // TODO: 待添加获取LLM的回复逻辑
        //添加知识库逻辑
        List<KnowledgeEntry> knowledgeEntries = ragQueryService.queryKnowledgeEntries(question,5);
        //格式化提问知识库内容
        String knowledgeText = ragQueryService.formatContextText(knowledgeEntries);
        //格式化知识库来源条目
        String sourceSummary  = ragQueryService.formatContextText(knowledgeEntries);

        //拼接问题上下文
        question += knowledgeText;

        String finalResponse;

        try{
            String llmResponse = chatClient.ask(question);
            //拼接大模型响应+知识库条目
            finalResponse = llmResponse + sourceSummary;
        }
        catch (Exception e) {
            finalResponse = "很抱歉，当前无法处理您的请求，请稍后再试。";
            // 记录详细错误日志，包含上下文
            LOG.error("调用大模型失败，问题内容: {}"+question, e);
        }

        return finalResponse;
    }


}
