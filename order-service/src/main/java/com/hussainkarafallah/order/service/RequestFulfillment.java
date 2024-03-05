package com.hussainkarafallah.order.service;

import org.springframework.stereotype.Service;

import com.hussainkarafallah.EventPublisher;
import com.hussainkarafallah.domain.MatchingType;
import com.hussainkarafallah.domain.OrderType;
import com.hussainkarafallah.interfaces.RequestMatchingEvent;
import com.hussainkarafallah.order.domain.StockOrder;
import com.hussainkarafallah.utils.UuidUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestFulfillment {

    private final EventPublisher eventPublisher;
    
    void exec(StockOrder order){
        RequestMatchingEvent event = RequestMatchingEvent.builder()
            .requestId(UuidUtils.generatePrefixCombUuid())
            .orderId(order.getId())
            .instrument(order.getInstrument().name())
            .price(order.getTargetPrice())
            .quantity(order.anticipatedFulfillment())
            .type(getMatchingType(order.getType()).name())
            .isPartial(order.getBasketId() != null)
            .build();
        log.info("requesting new fulfillment {} of {} for order {}", event.getQuantity(), event.getInstrument(), event.getOrderId());
        eventPublisher.publish(event);
    }

    MatchingType getMatchingType(OrderType orderType){
        return switch (orderType) {
            case BUY -> MatchingType.BUY;
            case SELL -> MatchingType.SELL;
            default -> throw new IllegalStateException("can only send requests to matching engine for BUY/SELL orders");
        };
    }
}
