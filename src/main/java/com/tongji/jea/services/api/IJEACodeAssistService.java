package com.tongji.jea.services.api;

import com.tongji.jea.model.ChatMessage;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 教学助手核心服务接口
 *
 * 这是前后端交互的唯一接口，定义了所有可用的操作
 *
 * @author 同济大学教学助手开发团队
 * @version 1.0
 */
public interface IJEACodeAssistService {

    /**
     * 向教学助手提问
     *
     * 该方法会：
     * 1. 自动添加上下文信息（当前选中的代码等）
     * 2. 从知识库检索相关内容
     * 3. 调用大语言模型生成回答
     * 4. 返回格式化响应（包含参考来源）
     *
     * @param question 用户提问内容，不能为空
     * @return 助手的回答，包含：
     *         - 针对问题的专业解答
     *         - 相关代码示例（纯文本格式）
     *         - 参考的知识库来源
     * @throws IllegalArgumentException 当问题为空时抛出
     *
     * 使用示例：
     * <pre>{@code
     * String answer = service.ask("如何实现Spring Boot的依赖注入？");
     * }</pre>
     */
    @NotNull
    String ask(@NotNull String question);

    /**
     * 清空当前对话历史
     *
     * 用于开始新的对话会话，清除之前的所有对话上下文
     *
     * 使用场景：
     * - 用户点击"新建对话"按钮
     * - 切换项目或工作空间时
     * - 需要重置对话状态时
     */
    void clearHistory();

    /**
     * 获取当前对话历史
     *
     * 返回当前会话中的所有对话消息，包括用户提问和助手回答
     * 返回的是副本，修改不会影响内部状态
     *
     * @return 对话历史列表，按时间顺序排列
     *         每条消息包含角色（user/assistant）和内容
     *
     * 使用示例：
     * <pre>{@code
     * List<ChatMessage> history = service.getHistory();
     * for (ChatMessage msg : history) {
     *     System.out.println(msg.getRole() + ": " + msg.getContent());
     * }
     * }</pre>
     */
    @NotNull
    List<ChatMessage> getHistory();

}