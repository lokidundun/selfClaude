package com.agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author loki1
 */
public class LLMClient {
    private static final Logger log = LoggerFactory.getLogger(LLMClient.class);
    private final String baseUrl;
    private final String apiKey;
    private final String model;
    private final ObjectMapper objectMapper;

    public LLMClient(String baseUrl, String apiKey, String model) {
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.model = model;
        this.objectMapper = new ObjectMapper();
    }

    public String chat(List<Message> messages) throws IOException {
        //不同 AI 服务商的 API 地址不同，根据自己所使用的 api 更改一下 url
        //对于 OpenAI: baseUrl + "/chat/completions"
        //对于 Anthropic 兼容 (MiniMax): baseUrl + "/v1/messages"
        String url = baseUrl + "/v1/messages";

        // Build request body
        StringBuilder requestBody = new StringBuilder();
        requestBody.append("{");
        requestBody.append("\"model\":\"").append(model).append("\",");
        requestBody.append("\"messages\":[");

        for (int i = 0; i < messages.size(); i++) {
            Message msg = messages.get(i);
            requestBody.append("{");
            requestBody.append("\"role\":\"").append(msg.role).append("\",");
            requestBody.append("\"content\":").append(objectMapper.writeValueAsString(msg.content));
            requestBody.append("}");
            if (i < messages.size() - 1) {
                requestBody.append(",");
            }
        }

        requestBody.append("],");
        requestBody.append("\"max_tokens\":4096,");
        requestBody.append("\"temperature\":0");
        requestBody.append("}");

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(url);
            post.setHeader("Authorization", "Bearer " + apiKey);
            post.setHeader("Content-Type", "application/json");
            post.setEntity(new StringEntity(requestBody.toString(), ContentType.APPLICATION_JSON));

            String response = client.execute(post, httpResponse -> {
                int statusCode = httpResponse.getCode();
                if (statusCode != 200) {
                    throw new IOException("HTTP error: " + statusCode);
                }
                return EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8);
            });


            // Parse response - try multiple formats
            JsonNode root = objectMapper.readTree(response);
            String content = null;

            // Try OpenAI format: choices[0].message.content
            JsonNode openAiContent = root.path("choices").path(0).path("message").path("content");
            if (openAiContent.isTextual()) {
                content = openAiContent.asText();
            }

            // Try MiniMax/Anthropic format: content is an array, find type="text"
            if (content == null || content.isEmpty()) {
                JsonNode contentArray = root.path("content");
                if (contentArray.isArray()) {
                    for (JsonNode item : contentArray) {
                        if ("text".equals(item.path("type").asText())) {
                            content = item.path("text").asText();
                            break;
                        }
                    }
                }
            }

            // Try simple text field
            if (content == null || content.isEmpty()) {
                content = root.path("text").asText();
            }

            if (content == null || content.isEmpty()) {
                throw new IOException("Could not find content in response: " + response);
            }

            return content;
        }
    }

    public static class Message {
        public final String role;
        public final String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }
}
