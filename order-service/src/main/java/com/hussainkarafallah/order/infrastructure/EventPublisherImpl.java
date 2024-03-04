package com.hussainkarafallah.order.infrastructure;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.hussainkarafallah.EventHandler;
import com.hussainkarafallah.EventPublisher;
import com.hussainkarafallah.interfaces.FulfillmentMatchedEvent;
import com.hussainkarafallah.interfaces.OrderUpdateEvent;
import com.hussainkarafallah.interfaces.RequestMatchingEvent;
import com.hussainkarafallah.interfaces.TradeEvent;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EventPublisherImpl implements EventPublisher{

    private List<EventHandler<FulfillmentMatchedEvent>> fulfillmentHandlers = new ArrayList<>();

    private List<EventHandler<RequestMatchingEvent>> matchingHandlers = new ArrayList<>();

    private List<EventHandler<TradeEvent>> tradeEventHandlers = new ArrayList<>();

    private List<EventHandler<OrderUpdateEvent>> orderUpdateHandlers = new ArrayList<>();

    @Override
    public void publish(FulfillmentMatchedEvent event) {
        fulfillmentHandlers.forEach(x -> x.onEvent(event));
    }

    @Override
    public void publish(RequestMatchingEvent event) {
        matchingHandlers.forEach(x -> x.onEvent(event));
    }

    @Override
    public void publish(TradeEvent event) {
        tradeEventHandlers.forEach(x -> x.onEvent(event));
    }

    @Override
    public void publish(OrderUpdateEvent event) {
        orderUpdateHandlers.forEach(x -> x.onEvent(event));
    }

    @Override
    public void subscribeFulfillment(EventHandler<FulfillmentMatchedEvent> handler) {
        this.fulfillmentHandlers.add(handler);
    }

    @Override
    public void subscribeMatchingRequest(EventHandler<RequestMatchingEvent> handler) {
        this.matchingHandlers.add(handler);
    }

    @Override
    public void subscribeTrade(EventHandler<TradeEvent> handler) {
        this.tradeEventHandlers.add(handler);
    }

    @Override
    public void subscribeOrderUpdate(EventHandler<OrderUpdateEvent> handler) {
        this.orderUpdateHandlers.add(handler);
    }
}
