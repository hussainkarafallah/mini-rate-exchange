package com.hussainkarafallah.order.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.stereotype.Component;

import com.hussainkarafallah.EventHandler;
import com.hussainkarafallah.EventPublisher;
import com.hussainkarafallah.interfaces.FulfillmentMatchedEvent;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class MatchingEventProcessor implements EventHandler<FulfillmentMatchedEvent>{
    
    private static int NUM_THREADS = 10;
    private static int max_capacity = 1000000;

    private final ExecutorService executorService;
    private final List<BlockingQueue<FulfillmentMatchedEvent>> queues;

    public MatchingEventProcessor(EventPublisher eventPublisher, MatchOrders matchOrders) {
        eventPublisher.subscribeFulfillment(this);
        executorService = Executors.newFixedThreadPool(NUM_THREADS);
        queues = new ArrayList<>();
        for(int i = 0 ; i < NUM_THREADS ; i++){
            queues.add(new ArrayBlockingQueue<>(max_capacity));
            executorService.submit(new OrderMatcher(queues.get(i), matchOrders));
        }
    }

    static class OrderMatcher implements Runnable {
        
        BlockingQueue<FulfillmentMatchedEvent> queue;
        MatchOrders matchOrders;

        OrderMatcher(BlockingQueue<FulfillmentMatchedEvent> queue, MatchOrders matchOrders){
            this.queue = queue;
            this.matchOrders = matchOrders;
        }

        @Override
        public void run(){
            while(true){
                List<FulfillmentMatchedEvent> events = new ArrayList<>();
                queue.drainTo(events);
                events.forEach(ev -> {
                    try{
                        matchOrders.match(ev);
                    } catch(Exception ex){
                        log.error("what is this even", ex);
                    }
                });
            }
        }
    
        
    }
    @Override
    public void onEvent(FulfillmentMatchedEvent event) {
        if(event != null){
            queues.get(event.getInstrument().hashCode() % NUM_THREADS).add(event);
        }
    }


}
