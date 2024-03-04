package com.hussainkarafallah;

import com.hussainkarafallah.interfaces.FulfillmentMatchedEvent;
import com.hussainkarafallah.interfaces.OrderUpdateEvent;
import com.hussainkarafallah.interfaces.RequestMatchingEvent;
import com.hussainkarafallah.interfaces.TradeEvent;

public interface EventPublisher {

    void subscribeFulfillment(EventHandler<FulfillmentMatchedEvent> handler);

    void subscribeMatchingRequest(EventHandler<RequestMatchingEvent> handler);

    void subscribeTrade(EventHandler<TradeEvent> handler);
    
    void subscribeOrderUpdate(EventHandler<OrderUpdateEvent> handler);

    void publish(FulfillmentMatchedEvent event);

    void publish(RequestMatchingEvent event);

    void publish(TradeEvent event);
    
    void publish(OrderUpdateEvent event);

}
