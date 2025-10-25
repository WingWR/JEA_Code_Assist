package com.tongji.jea.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;

public class DashScopeExecutor {

    /** DashScope 官方基础 URL */
    private static final String BASE_URL = "https://dashscope.aliyuncs.com";

    /** HTTP 客户端 */
    private final OkHttpClient httpClient;

    /** JSON 处理器 */
    private final ObjectMapper objectMapper;

    /** 阿里云 API Key */
    private final String apiKey;

    /**
     * 构造函数。
     * @param apiKey 阿里云的apiKey
     */
    public DashScopeExecutor(String apiKey) {
        this.apiKey = apiKey;
        this.objectMapper = new ObjectMapper();
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(20))
                .readTimeout(Duration.ofSeconds(60))
                .build();
    }

    /**
     *  执行Post请求
     *
     * @param path     例如 "/api/v1/services/embeddings/text-embedding-v4"
     * @param jsonBody 请求体（JsonNode）
     * @return 响应体（JsonNode）
     * @throws IOException 网络或解析异常
     */
    public JsonNode executePost(String path, JsonNode jsonBody) throws IOException {
        String url = BASE_URL + path;

        String jsonString = objectMapper.writeValueAsString(jsonBody);
        RequestBody body = RequestBody.create(
                jsonString,
                MediaType.parse("application/json; charest=utf-8")
        );

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization","Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        try(Response response = httpClient.newCall(request).execute()) {
            String responseBody = response != null ? response.body().string():"";
            if(!response.isSuccessful()){
                throw new IOException(String.format(
                        "DashScope API call failed(%d): %s", response.code(), responseBody
                ));
            }
            return objectMapper.readTree(responseBody);
        }

    }
    /**
     * 执行 GET 请求。
     *
     * @param path   URL 路径（不含 BASE_URL）
     * @param params 可选查询参数
     * @return 响应体（JsonNode）
     * @throws IOException 网络或解析异常
     */
    public JsonNode executeGet(String path, Map<String, String> params) throws IOException {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(BASE_URL + path).newBuilder();
        if (params != null) {
            params.forEach(urlBuilder::addQueryParameter);
        }

        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .addHeader("Authorization", "Bearer " + apiKey)
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String respBody = response.body() != null ? response.body().string() : "";
            if (!response.isSuccessful()) {
                throw new IOException(String.format(
                        "DashScope GET failed (%d): %s", response.code(), respBody));
            }
            return objectMapper.readTree(respBody);
        }
    }

    /**
     * 获取 ObjectMapper，用于上层构造 JSON。
     */
    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    /**
     * 关闭底层 HTTP 客户端。
     * （通常无需主动调用）
     */
    public void close() {
        httpClient.connectionPool().evictAll();
    }


}
