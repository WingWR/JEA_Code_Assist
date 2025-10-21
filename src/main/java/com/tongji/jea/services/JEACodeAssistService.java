package com.tongji.jea.services;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;

@Service(Service.Level.PROJECT)
public final class JEACodeAssistService {
    private static final Logger LOG = Logger.getInstance(JEACodeAssistService.class);

    public JEACodeAssistService(Project project) {
        LOG.info("JEACodeAssistService initialized for project: " + project.getName());
    }

    public String askTA(String question) {
        // 模拟后台回答逻辑
        return "You asked: " + question + "\nResponse: (dummy TA reply)";
    }
}
