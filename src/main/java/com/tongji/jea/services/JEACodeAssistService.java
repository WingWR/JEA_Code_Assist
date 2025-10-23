package com.tongji.jea.services;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * 后台服务，处理与教学助手（TA）的交互
 * 即后端与前端的交互点
 */
@Service(Service.Level.PROJECT)
public final class JEACodeAssistService {
    private static final Logger LOG = Logger.getInstance(JEACodeAssistService.class);

    public JEACodeAssistService(Project project) {
        LOG.info("JEACodeAssistService initialized for project: " + project.getName());
    }

    public String askTA(@NotNull String question) {
        LOG.info("AskTA called with question: " + question);
        String answer = new LLMService().ask(question);
        LOG.info("Answer: " + answer);
        return answer;
    }
}
