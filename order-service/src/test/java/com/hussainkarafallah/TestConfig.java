package com.hussainkarafallah;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestConfig {
    @Bean
    public EventPublisher eventPublisher(RecordingEventPublisher recordingEventPublisher){
        return recordingEventPublisher;
    }
}
