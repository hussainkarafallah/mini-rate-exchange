package com.hussainkarafallah.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hussainkarafallah.utils.ExceptionUtils;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class ObjectMapperConfiguration {
    private final static ObjectMapper objectMapper = createObjectMapper();

    public static byte[] toBytes(Object obj){
        return ExceptionUtils.doUnchecked(() -> objectMapper.writeValueAsBytes(obj));
    }

    public static <T> T fromBytes(byte[] bytes, TypeReference<T> ref){
        return ExceptionUtils.doUnchecked(() -> objectMapper.readValue(bytes, ref));
    }

    public static <T> T fromBytes(byte[] bytes, Class<T> clazz){
        return ExceptionUtils.doUnchecked(() -> objectMapper.readValue(bytes, clazz));
    }

    private static ObjectMapper createObjectMapper(){
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new Jdk8Module());
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return objectMapper;
    }

    @Bean
    @Primary
    public ObjectMapper objectMapper(){
        return objectMapper;
    }


}
