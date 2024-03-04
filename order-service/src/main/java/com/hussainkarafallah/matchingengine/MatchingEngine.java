package com.hussainkarafallah.matchingengine;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.BlockingQueue;

import com.hussainkarafallah.EventPublisher;
import com.hussainkarafallah.domain.MatchingType;
import com.hussainkarafallah.interfaces.FulfillmentMatchedEvent;
import com.hussainkarafallah.matchingengine.domain.MatchingRequest;
import com.hussainkarafallah.utils.UuidUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MatchingEngine implements Runnable {
    private final PriorityQueue<MatchingRequest> sellOrdersPool;
    private final PriorityQueue<MatchingRequest> pendingBuyOrders;
    private final PriorityQueue<MatchingRequest> sellOrdersWithNoPrice;
    private final EventPublisher eventPublisher;
    private final BlockingQueue<MatchingRequest> queue;

    public MatchingEngine(EventPublisher eventPublisher, BlockingQueue<MatchingRequest> queue){
        Comparator<MatchingRequest> orderByOrderId = Comparator.comparing(MatchingRequest::getOrderId);
        sellOrdersPool = new PriorityQueue<>(Comparator.comparing(MatchingRequest::getPrice).thenComparing(MatchingRequest::getOrderId));
        pendingBuyOrders = new PriorityQueue<>(orderByOrderId);
        sellOrdersWithNoPrice = new PriorityQueue<>(orderByOrderId);
        this.eventPublisher = eventPublisher;
        this.queue = queue;
    }

    @Override
    public void run() {
        while(true){
            List<MatchingRequest> orders = new ArrayList<>();
            queue.drainTo(orders);
            orders.forEach(order -> {
                log.info("Engine:: Accepted order into the engine  {} , {} , {}, {} , {}", order.getOrderId(), order.getType(), order.getInstrument(), order.getQuantity(), order.getPrice());
                if (MatchingType.BUY.equals(order.getType())) {
                    acceptBuyOrder(order);
                } else if (MatchingType.SELL.equals(order.getType())) {
                    acceptSellOrder(order);
                }
            });
        }
    }


    public void acceptBuyOrder(MatchingRequest buyOrder) {
        // first we try to match with the best price possible
        if (!sellOrdersPool.isEmpty() && canMatch(buyOrder, sellOrdersPool.peek())) {
            match(buyOrder , sellOrdersPool.poll());
        }
        // we try to match with a sell order with no price 
        else if(!sellOrdersWithNoPrice.isEmpty()) {
            match(buyOrder , sellOrdersWithNoPrice.poll());
        }
        // we could not match and we add to our waiting list
        else {
            pendingBuyOrders.add(buyOrder);
        }
    }

    public void acceptSellOrder(MatchingRequest sellOrder) {
        Iterator<MatchingRequest> iterator = pendingBuyOrders.iterator();
        // a sell order must always go to the pool unless we have pending buy orders
        while (iterator.hasNext()) {
            MatchingRequest buyOrder = iterator.next();
            if (canMatch(buyOrder, sellOrder)) {
                match(buyOrder, sellOrder);
                iterator.remove();
                break;
            }
        }
        if(sellOrder.getPrice() == null){
            sellOrdersWithNoPrice.add(sellOrder);
        }
        else{
            sellOrdersPool.add(sellOrder);
        }
    }

    private boolean canMatch(MatchingRequest buyOrder, MatchingRequest sellOrder){
        if(buyOrder.getPrice() == null && sellOrder.getPrice() == null){
            return false;
        }
        if(buyOrder.getPrice() == null || sellOrder.getPrice() == null){
            return true;
        }
        return buyOrder.getPrice().compareTo(sellOrder.getPrice()) >= 0;
    }

    private void match(MatchingRequest buyOrder, MatchingRequest sellOrder){
        BigDecimal price = sellOrder.getPrice() == null ? buyOrder.getPrice() : sellOrder.getPrice();
        BigDecimal quantity = buyOrder.getQuantity().min(sellOrder.getQuantity());
        FulfillmentMatchedEvent event = FulfillmentMatchedEvent.builder()
            .matchId(UuidUtils.generatePrefixCombUuid())
            .instrument(buyOrder.getInstrument())
            .buyFulfillmentId(buyOrder.getRequestId())
            .buyOrderId(buyOrder.getOrderId())
            .sellFulfillmentId(sellOrder.getRequestId())
            .sellOrderId(sellOrder.getOrderId())
            .quantity(quantity)
            .price(price)
            .build();
        log.info("Engine:: Matched {} , {} , {} of {}" , buyOrder.getOrderId() , sellOrder.getOrderId(), quantity, buyOrder.getInstrument());
        eventPublisher.publish(event);
    }


    
}
