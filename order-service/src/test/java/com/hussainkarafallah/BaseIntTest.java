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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import com.hussainkarafallah.domain.OrderState;
import com.hussainkarafallah.interfaces.FulfillmentMatchedEvent;
import com.hussainkarafallah.order.domain.CompositeOrder;
import com.hussainkarafallah.order.domain.StockOrder;
import com.hussainkarafallah.order.repository.OrderRepository;
import com.hussainkarafallah.order.repository.PriceBook;
import com.hussainkarafallah.order.service.CreateOrder;
import com.hussainkarafallah.order.service.commands.CreateOrderCommand;

import lombok.SneakyThrows;

@SpringBootTest
@ContextConfiguration(classes = {TestApplication.class, TestConfig.class, RecordingEventPublisher.class})
@ActiveProfiles("test")
@DirtiesContext
public abstract class BaseIntTest {

    @Autowired
    protected CreateOrder createOrder;

    @Autowired
    protected OrderRepository orderRepository;

    @Autowired
    protected PriceBook priceBook;

    @Autowired
    RecordingEventPublisher eventPublisher;


    
    @BeforeEach
    @SneakyThrows
    void init(){
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

    protected FulfillmentMatchedEvent awaitMatchBetween(StockOrder buyorder , StockOrder sellorder){
        return awaitMessageSent(
            FulfillmentMatchedEvent.class,
            ev -> ev.getBuyOrderId().equals(buyorder.getId())
            && ev.getSellOrderId().equals(sellorder.getId())
        );
    }

    protected FulfillmentMatchedEvent awaitMatchBetween(StockOrder buyorder , UUID sellId){
        var sellOrder = orderRepository.findById(sellId).orElseThrow();
        return awaitMessageSent(
            FulfillmentMatchedEvent.class,
            ev -> ev.getBuyOrderId().equals(buyorder.getId())
            && ev.getSellOrderId().equals(sellOrder.getId())
        );
    }

    protected StockOrder createOrder(CreateOrderCommand cmd){
        var id = createOrder.exec(cmd);
        return orderRepository.findById(id).orElseThrow();
    }

    protected CompositeOrder createCompositeOrder(CreateOrderCommand cmd){
        var id = createOrder.exec(cmd);
        return orderRepository.findBasketById(id).orElseThrow();
    }


}
