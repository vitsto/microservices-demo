package com.microservices.demo.ai.generated.tweet.to.kafka.service;

import com.microservices.demo.config.AIGeneratedTweetToKafkaServiceConfigData;
import com.microservices.demo.ai.generated.tweet.to.kafka.service.init.StreamInitializer;
import com.microservices.demo.ai.generated.tweet.to.kafka.service.runner.AIStreamRunner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Slf4j
@SpringBootApplication
@ComponentScan(basePackages = "com.microservices.demo")
@EnableScheduling
public class AIGeneratedTweetToKafkaServiceApplication implements CommandLineRunner {

    private final AIGeneratedTweetToKafkaServiceConfigData configData;
    private final StreamInitializer streamInitializer;
    private final AIStreamRunner aiStreamRunner;
    private final TaskScheduler taskScheduler;

    public AIGeneratedTweetToKafkaServiceApplication(AIGeneratedTweetToKafkaServiceConfigData configData,
                                                     StreamInitializer streamInitializer, AIStreamRunner aiStreamRunner,
                                                     TaskScheduler taskScheduler) {
        this.configData = configData;
        this.streamInitializer = streamInitializer;
        this.aiStreamRunner = aiStreamRunner;
        this.taskScheduler = taskScheduler;
    }

    static void main(String[] args) {
        SpringApplication.run(AIGeneratedTweetToKafkaServiceApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Application is starting...");
        boolean initResult = streamInitializer.init();
        if (initResult) {
            log.info("Starting AI Stream Runner with fixed rate {} seconds!", configData.getSchedulerDurationSec());
            taskScheduler.scheduleAtFixedRate(aiStreamRunner, Duration.of(configData.getSchedulerDurationSec(), ChronoUnit.SECONDS));
        } else {
            log.error("Stream Initializer failed to initialize the streams! Not starting the AI Stream Runner!");
        }
    }
}
