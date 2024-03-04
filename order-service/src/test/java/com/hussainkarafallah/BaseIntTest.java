package com.hussainkarafallah;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.jdbc.JdbcTestUtils;

import com.hussainkarafallah.domain.OrderState;
import com.hussainkarafallah.interfaces.FulfillmentMatchedEvent;
import com.hussainkarafallah.order.domain.Order;
import com.hussainkarafallah.order.repository.OrderRepository;
import com.hussainkarafallah.order.repository.PriceBook;
import com.hussainkarafallah.order.service.CreateOrder;

import lombok.SneakyThrows;

@SpringBootTest
@ContextConfiguration(classes = {TestApplication.class, TestConfig.class, RecordingEventPublisher.class})
@ActiveProfiles("test")
public abstract class BaseIntTest {

    @Autowired
    protected CreateOrder createOrder;

    @Autowired
    protected OrderRepository orderRepository;


    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TestKafkaConsumer testKafkaConsumer;

    @Autowired
    protected PriceBook priceBook;

    @Autowired
    RecordingEventPublisher eventPublisher;


    
    @BeforeEach
    @SneakyThrows
    void init(){
        JdbcTestUtils.deleteFromTables(jdbcTemplate,"orders");
        //testKafkaConsumer.reset();
    }


    public static BigDecimal aDecimal(double v) {
        return BigDecimal.valueOf(v);
    }

    protected <T> T awaitMessageSent(Class<T> clazz, Predicate<T> predicate) {
        return Awaitility.await().until(
            () -> eventPublisher.findMessage(clazz, predicate),
            Optional::isPresent
        ).orElseThrow();
    }


    protected void verifyOrderState(UUID orderId , OrderState orderState){
        assertEquals(orderState , orderRepository.findById(orderId).orElseThrow().getState());
    }

    protected FulfillmentMatchedEvent awaitMatchBetween(Order buyorder , Order sellorder){
        return awaitMessageSent(
            FulfillmentMatchedEvent.class,
            ev -> ev.getBuyOrderId().equals(buyorder.getId())
            && ev.getSellOrderId().equals(sellorder.getId())
        );

    }


}
