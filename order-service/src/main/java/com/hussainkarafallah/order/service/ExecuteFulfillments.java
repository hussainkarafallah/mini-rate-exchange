package com.hussainkarafallah.order.service;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.hussainkarafallah.domain.FulfillmentState;
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
public class ExecuteFulfillments {
    
    private final OrderRepository orderRepository;

    private final PublishOrderUpdate publishOrderUpdate;

    private final ExecuteTrade executeTrade;

    void tryExecute(StockOrder order){

        if(canExecute(order)){
            for(Fulfillment fulfillment : order.getFulfillments()){
                if(!fulfillment.getState().equals(FulfillmentState.MATCHED)){
                    continue;
                }
                executeFulfillment(order, fulfillment);
                StockOrder matchingOrder = orderRepository.findById(fulfillment.getFulfillerId()).orElseThrow();
                Fulfillment matchingFulfillment = matchingOrder.getFulfillments().stream()
                    .filter(f -> f.getId().equals(fulfillment.getId()))
                    .findAny()
                    .orElseThrow();
                executeFulfillment(matchingOrder, matchingFulfillment);
                executeTrade.executeTrade(order.getInstrument(), fulfillment.getFulfilledPrice(), order.getTraderId(), matchingOrder.getTraderId());
            }
        }

    }

    private boolean canExecute(StockOrder order){
        if(order.getBasketId() == null){
            return true;
        }
        return orderRepository.findBasketById(order.getBasketId())
            .orElseThrow()
            .getStockOrdersIds()
            .stream()
            .map(orderRepository::findById)
            .map(Optional::orElseThrow)
            .allMatch(x -> x.anticipatedFulfillment().compareTo(BigDecimal.ZERO) == 0);
    }

    private void executeFulfillment(StockOrder order , Fulfillment fulfillment){
        log.info("Executing fulfillment {} of {} at {} for {}", fulfillment.getFulfilledQuantity(), order.getInstrument(), fulfillment.getFulfilledPrice(), order.getId());
        OrderSnapshot beforeExecutiOrderSnapshot = OrderMapper.toOrderSnapshot(order);

        fulfillment.setState(FulfillmentState.EXECUTED);
        if(order.canBeClosed()){
            log.info("Closing order {}" , order.getId());
            order.close();
        }

        orderRepository.save(order);
        publishOrderUpdate.onOrderUpdated(beforeExecutiOrderSnapshot, OrderMapper.toOrderSnapshot(order));
    }



}
