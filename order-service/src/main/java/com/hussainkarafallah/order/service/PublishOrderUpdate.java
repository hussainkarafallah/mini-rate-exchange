package com.hussainkarafallah.order.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.hussainkarafallah.EventPublisher;
import com.hussainkarafallah.interfaces.OrderSnapshot;
import com.hussainkarafallah.interfaces.OrderUpdateEvent;
import com.hussainkarafallah.order.domain.StockOrder;
import com.hussainkarafallah.order.mappers.OrderMapper;
import com.hussainkarafallah.utils.UuidUtils;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PublishOrderUpdate {

    private final EventPublisher eventPublisher;

    public void onOrderCreated(@NonNull StockOrder order) {
        onOrderUpdated(null, OrderMapper.toOrderSnapshot(order));
    }

    public void onOrderUpdated(OrderSnapshot oldOrder, @NonNull OrderSnapshot newOrder) {
        UUID eventId = UuidUtils.generatePrefixCombUuid();
        OrderUpdateEvent event = OrderUpdateEvent.builder()
                .eventId(eventId)
                .oldState(oldOrder == null ? null : oldOrder.getState())
                .newState(newOrder.getState())
                .snapshot(newOrder)
                .build();
        eventPublisher.publish(event);
    }

    

    
}
