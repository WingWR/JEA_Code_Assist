package com.tongji.jea.services;

import org.json.JSONObject;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import org.json.JSONArray;

public class AliyunLLMClient {

    private static final String API_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";
    private static final String API_KEY = "sk-7d351140e99c4744a37d73b67bfe7592";

    public String askLLM(String question) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(API_URL);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(60000);
            conn.setDoOutput(true);

            // 构建正确的请求体
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", "qwen-turbo");

            JSONObject input = new JSONObject();
            input.put("prompt", question);
            requestBody.put("input", input);

            JSONObject parameters = new JSONObject();
            parameters.put("stream", false);// 非流式输出
            requestBody.put("parameters", parameters);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(requestBody.toString().getBytes(StandardCharsets.UTF_8));
            }

            int code = conn.getResponseCode();
            String responseContent;
            try (Scanner scanner = new Scanner(
                    code == 200 ? conn.getInputStream() : conn.getErrorStream(),
                    StandardCharsets.UTF_8
            ).useDelimiter("\\A")) {
                responseContent = scanner.hasNext() ? scanner.next() : "";
            }

            if (code != 200) {
                return "API Error: " + code + " - " + responseContent;
            }

            JSONObject result = new JSONObject(responseContent);
            return parseResponse(result);

        } catch (Exception e) {
            e.printStackTrace();
            return "Error calling LLM: " + e.getMessage();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private String parseResponse(JSONObject result) {
        try {
            if (result.has("output")) {
                JSONObject output = result.getJSONObject("output");
                if (output.has("choices")) {
                    JSONArray choices = output.getJSONArray("choices");
                    if (choices.length() > 0) {
                        JSONObject choice = choices.getJSONObject(0);
                        if (choice.has("message")) {
                            JSONObject message = choice.getJSONObject("message");
                            return message.optString("content", "No content in message.");
                        }
                    }
                }
                // 兼容旧版本API
                return output.optString("text", "No response from model.");
            }
            return "No output in response.";
        } catch (Exception e) {
            return "Error parsing response: " + e.getMessage();
        }
    }
}
