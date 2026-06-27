package com.microservices.demo.ai.generated.tweet.to.kafka.service.service.openai;

import com.microservices.demo.config.AIGeneratedTweetToKafkaServiceConfigData;
import com.microservices.demo.ai.generated.tweet.to.kafka.service.exception.AIGeneratedTweetToKafkaServiceException;
import com.microservices.demo.ai.generated.tweet.to.kafka.service.service.AIService;
import com.microservices.demo.ai.generated.tweet.to.kafka.service.service.openai.model.OpenAIRequest;
import com.microservices.demo.ai.generated.tweet.to.kafka.service.service.openai.model.OpenAIResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

@Service
@ConditionalOnProperty(name = "ai-generated-tweet-to-kafka-service.ai-service",
        havingValue = "OpenAI")
@Slf4j
public class OpenAIService implements AIService {

    private final AIGeneratedTweetToKafkaServiceConfigData configData;
    private final ObjectMapper objectMapper;

    public OpenAIService(AIGeneratedTweetToKafkaServiceConfigData configData, ObjectMapper objectMapper) {
        this.configData = configData;
        this.objectMapper = objectMapper;
    }

    @Override
    public String generateTweet() throws AIGeneratedTweetToKafkaServiceException {
        log.info("Generating tweet using OpenAIService");
        String prompt = configData.getPrompt().replace(configData.getKeywordsPlaceholder(),
                String.join(", ", configData.getStreamingDataKeywords()));

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost request = getRequest(prompt);
            String response = httpClient.execute(request, resp -> EntityUtils.toString(resp.getEntity()));
            return parseResponse(response);
        } catch (IOException e) {
            throw new AIGeneratedTweetToKafkaServiceException("Failed to generate tweet from OpenAI", e);
        }
    }

    private HttpPost getRequest(String prompt) {
        HttpPost request = new HttpPost(configData.getOpenAI().getUrl());
        request.addHeader(HttpHeaders.CONTENT_TYPE, configData.getOpenAI().getContentType());
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + configData.getOpenAI().getApiKey());
        OpenAIRequest openAIRequest = OpenAIRequest.builder()
                .model(configData.getOpenAI().getModel())
                .max_completion_tokens(configData.getOpenAI().getMaxCompletionTokens())
                .temperature(configData.getOpenAI().getTemperature())
                .messages(configData.getOpenAI().getMessages().stream()
                        .map(message -> OpenAIRequest.Message.builder()
                                .role(message.getRole())
                                .content(List.of(OpenAIRequest.Content.builder()
                                        .type(message.getContent().getFirst().getType())
                                        .text(prompt)
                                        .build())
                                )
                                .build()).toList())
                .build();

        request.setEntity(new StringEntity(objectMapper.writeValueAsString(openAIRequest)));
        return request;
    }

    private String parseResponse(String response) {
        OpenAIResponse openAIResponse = objectMapper.readValue(response, OpenAIResponse.class);
        return openAIResponse.getChoices().getFirst().getMessage().getContent();
    }
}
