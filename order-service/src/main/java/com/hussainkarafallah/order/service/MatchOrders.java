package com.hussainkarafallah.order.service;

import org.springframework.stereotype.Service;

import com.hussainkarafallah.domain.FulfillmentState;
import com.hussainkarafallah.interfaces.FulfillmentMatchedEvent;
import com.hussainkarafallah.order.domain.Order;
import com.hussainkarafallah.order.repository.OrderRepository;
import com.hussainkarafallah.order.service.commands.FulfillOrderCommand;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchOrders {

    private final OrderRepository orderRepository;

    private final FulfillOrder fulfillOrder;

    private final ExecuteFulfillments executeFulfillments;

    public void match(FulfillmentMatchedEvent event){
        Order buyOrder = orderRepository.findById(event.getBuyOrderId()).orElseThrow();
        Order sellOrder = orderRepository.findById(event.getSellOrderId()).orElseThrow();
        var buyOrderFulfillment = buyOrder.getFulfillments().stream()
            .filter(x -> x.getId().equals(event.getBuyFulfillmentId())).findAny().orElseThrow();
        var sellOrderFulfillment = sellOrder.getFulfillments().stream()
            .filter(x -> x.getId().equals(event.getSellFulfillmentId())).findAny().orElseThrow();
        
        log.info("attempting to match orders {} , {} with {} of {} priced at {}", buyOrder.getId(), sellOrder.getId(), event.getQuantity(), buyOrderFulfillment.getInstrument(), event.getPrice());
        if(buyOrderFulfillment.getState().equals(FulfillmentState.NOT_COMPLETED) && sellOrderFulfillment.getState().equals(FulfillmentState.NOT_COMPLETED)){
            fulfillOrder.exec(FulfillOrderCommand.builder()
                .order(buyOrder)
                .fulfillment(buyOrderFulfillment)
                .fulfillerId(sellOrder.getId())
                .quantity(event.getQuantity())
                .price(event.getPrice())
                .build()
            );

            fulfillOrder.exec(FulfillOrderCommand.builder()
                .order(sellOrder)
                .fulfillment(sellOrderFulfillment)
                .fulfillerId(buyOrder.getId())
                .quantity(event.getQuantity())
                .price(event.getPrice())
                .build()
            );

            executeFulfillments.tryExecute(sellOrder);
            executeFulfillments.tryExecute(buyOrder);

            
        }


    }


}
