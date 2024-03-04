package com.hussainkarafallah.order.service;

import org.springframework.stereotype.Service;

import com.hussainkarafallah.domain.FulfillmentState;
import com.hussainkarafallah.domain.OrderState;
import com.hussainkarafallah.interfaces.OrderSnapshot;
import com.hussainkarafallah.order.domain.Fulfillment;
import com.hussainkarafallah.order.domain.Order;
import com.hussainkarafallah.order.mappers.OrderMapper;
import com.hussainkarafallah.order.repository.OrderRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExecuteFulfillments {
    
    private final OrderRepository orderRepository;

    private final PublishOrderUpdate publishOrderUpdate;

    void tryExecute(Order order){
        if(!order.canExecute()){
            return;
        }

        for(Fulfillment fulfillment : order.getFulfillments()){
            
            if(!fulfillment.getState().equals(FulfillmentState.FULFILLED)){
                continue;
            }
            
            executeFulfillment(order, fulfillment);
            
            Order matchingOrder = orderRepository.findById(fulfillment.getFulfillerId()).orElseThrow();
            Fulfillment matchingFulfillment = matchingOrder.getFulfillments().stream()
                .filter(f -> order.getId().equals(f.getFulfillerId()) && fulfillment.getInstrument().equals(f.getInstrument()))
                .findAny()
                .orElseThrow();
            executeFulfillment(matchingOrder, matchingFulfillment);

        }

    }

    private void executeFulfillment(Order order , Fulfillment fulfillment){
        log.info("Executing fulfillment {} of {} at {} for {}", fulfillment.getFulfilledQuantity(), fulfillment.getInstrument(), fulfillment.getFulfilledQuantity(), order.getId());
        OrderSnapshot beforeExecutiOrderSnapshot = OrderMapper.toOrderSnapshot(order);

        fulfillment.setState(FulfillmentState.EXECUTED);
        if(order.canBeClosed()){
            log.info("Closing order {}" , order.getId());
            order.setState(OrderState.CLOSED);
        }

        orderRepository.save(order);
        publishOrderUpdate.onOrderUpdated(beforeExecutiOrderSnapshot, OrderMapper.toOrderSnapshot(order));
    }



}
