package com.microservices.demo.ai.generated.tweet.to.kafka.service.service.springai;

import com.microservices.demo.config.AIGeneratedTweetToKafkaServiceConfigData;
import com.microservices.demo.ai.generated.tweet.to.kafka.service.service.AIService;
import com.microservices.demo.ai.generated.tweet.to.kafka.service.service.springai.model.TweetResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;

import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@ConditionalOnProperty(name = "ai-generated-tweet-to-kafka-service.ai-service", havingValue = "SpringAI-DeepSeek")
public class SpringAIDeepSeekService implements AIService {

    private final ChatClient ollamaChatClient;
    private final AIGeneratedTweetToKafkaServiceConfigData configData;

    @Value("classpath:templates/tweet-prompt.st")
    private Resource tweetPrompt;

    public SpringAIDeepSeekService(ChatClient ollamaChatClient, AIGeneratedTweetToKafkaServiceConfigData configData) {
        this.ollamaChatClient = ollamaChatClient;
        this.configData = configData;
    }

    @Override
    public String generateTweet() {
        log.info("Generating tweet using Spring AI Ollama service");

        BeanOutputConverter<TweetResponse> converter =
                new BeanOutputConverter<>(TweetResponse.class);

        PromptTemplate promptTemplate = new PromptTemplate(tweetPrompt);
        Prompt prompt = promptTemplate.create(Map.of(
                configData.getKeywordsPlaceholder().replace("{", "").replace("}", ""),
                String.join(",", configData.getStreamingDataKeywords()),
                "format", converter.getFormat()));

        String modelResult = ollamaChatClient.prompt(prompt).call().content();
        log.info("Model result: {}", modelResult);
        return modelResult.trim();
    }
}
