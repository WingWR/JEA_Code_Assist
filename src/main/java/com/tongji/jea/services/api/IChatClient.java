package com.tongji.jea.services.api;

/**
 * 与大语言模型交互的统一接口。
 */
public interface IChatClient {
    /**
     * 向模型发送问题并获取回答。
     * @param question 用户问题
     * @return 模型返回的回答文本
     * @throws Exception 调用失败或解析异常
     */
    String ask(String question) throws Exception;
}
