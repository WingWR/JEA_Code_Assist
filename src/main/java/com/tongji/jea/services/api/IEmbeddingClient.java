package com.tongji.jea.services.api;

import java.util.List;

/**
 * 文本向量化客户端接口。
 */
public interface IEmbeddingClient {
    /**
     * 获取文本的向量表示。
     * @param text 输入文本
     * @return 向量列表
     * @throws Exception 调用失败或解析异常
     */
    List<Double> getEmbedding(String text) throws Exception;
}
