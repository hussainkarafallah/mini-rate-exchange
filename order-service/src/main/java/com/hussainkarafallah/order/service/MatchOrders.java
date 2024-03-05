package com.hussainkarafallah.order.service;

import java.math.BigDecimal;
import java.time.Instant;

import org.springframework.stereotype.Service;

import com.hussainkarafallah.domain.FulfillmentState;
import com.hussainkarafallah.interfaces.FulfillmentMatchedEvent;
import com.hussainkarafallah.interfaces.OrderSnapshot;
import com.hussainkarafallah.order.domain.Fulfillment;
import com.hussainkarafallah.order.domain.StockOrder;
import com.hussainkarafallah.order.mappers.OrderMapper;
import com.hussainkarafallah.order.repository.OrderRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchOrders {

    private final OrderRepository orderRepository;

    private final PublishOrderUpdate publishOrderUpdate;

    private final RequestFulfillment requestFulfillment;

    private final ExecuteFulfillments executeFulfillments;

    public void match(FulfillmentMatchedEvent event){

        StockOrder buyOrder = orderRepository.findById(event.getBuyOrderId()).orElseThrow();
        StockOrder sellOrder = orderRepository.findById(event.getSellOrderId()).orElseThrow();
        
        if(buyOrder.getFulfillments().stream().anyMatch(f -> f.getId().equals(event.getMatchId()))){
            return;
        }
        if(sellOrder.getFulfillments().stream().anyMatch(f -> f.getId().equals(event.getMatchId()))){
            return;
        }

        log.info("matching orders {} , {} with {} of {} priced at {}", buyOrder.getId(), sellOrder.getId(), event.getQuantity(), buyOrder.getInstrument(), event.getPrice());
        
        
        fulfill(sellOrder, event);
        fulfill(buyOrder, event);
        
        executeFulfillments.tryExecute(sellOrder);
        executeFulfillments.tryExecute(buyOrder);
        
    }

    private void fulfill(StockOrder order, FulfillmentMatchedEvent event){
        
        log.info("fulfilling order {} with {} of {} at {}", order.getId(), event.getQuantity(), order.getInstrument(), event.getPrice());


        OrderSnapshot beforeFulfullSnapshot = OrderMapper.toOrderSnapshot(order);
        order.addFulfillment(
            Fulfillment.builder()
                .id(event.getMatchId())
                .state(FulfillmentState.MATCHED)
                .fulfilledPrice(event.getPrice())
                .fulfilledQuantity(event.getQuantity())
                .fulfillerId(event.getBuyOrderId().equals(order.getId()) ? event.getSellOrderId() : event.getBuyOrderId())
                .dateUpdated(Instant.now())
                .build()
        );

        if(order.anticipatedFulfillment().compareTo(BigDecimal.ZERO) > 0){
            log.info("anticipation is {}" , order.anticipatedFulfillment().toEngineeringString());
            requestFulfillment.exec(order);
        }

        orderRepository.save(order);
        publishOrderUpdate.onOrderUpdated(beforeFulfullSnapshot, OrderMapper.toOrderSnapshot(order));
    }


}
