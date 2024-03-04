package com.hussainkarafallah.matchingengine;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.hussainkarafallah.EventHandler;
import com.hussainkarafallah.EventPublisher;
import com.hussainkarafallah.domain.Instrument;
import com.hussainkarafallah.domain.MatchingType;
import com.hussainkarafallah.interfaces.RequestMatchingEvent;
import com.hussainkarafallah.matchingengine.domain.MatchingRequest;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class MatchingRequestProcessor implements EventHandler<RequestMatchingEvent> {

    private final Cache<UUID, Boolean> processedOrdersCache;
    private final ExecutorService executorService;
    private final List<BlockingQueue<MatchingRequest>> queues;

    private static int NUM_THREADS = 10;
    private static int max_capacity = 1000000;

    public MatchingRequestProcessor(EventPublisher eventPublisher) {
        eventPublisher.subscribeMatchingRequest(this);
        this.processedOrdersCache = Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.SECONDS)
                .build();
        executorService = Executors.newFixedThreadPool(NUM_THREADS);
        queues = new ArrayList<>();
        for(int i = 0 ; i < NUM_THREADS ; i++){
            queues.add(new ArrayBlockingQueue<>(max_capacity));
            executorService.submit(new MatchingEngine(eventPublisher, queues.get(i)));
        }
    }

    @Override
    public void onEvent(RequestMatchingEvent request) {
        /*
            this does not guarantee idempotency but meant for optimization and removing duplicates
            because regular messaging systems are at least once delivery. Our order logic must handle matches idempotently
            Also in case of incidents the cache might be cleared
        */
        if (processedOrdersCache.getIfPresent(request.getRequestId()) == null) {
            processedOrdersCache.put(request.getRequestId(), true);
            queues.get(request.getInstrument().hashCode() % NUM_THREADS).add(toMatchingRequest(request));
        }
    }

    private MatchingRequest toMatchingRequest(RequestMatchingEvent event){
        try{
            return MatchingRequest.builder()
                .requestId(event.getRequestId())
                .orderId(event.getOrderId())
                .instrument(Instrument.valueOf(event.getInstrument()))
                .price(event.getPrice())
                .quantity(event.getQuantity())
                .type(MatchingType.valueOf(event.getType()))
                .build();
        } catch(Exception ex){
            log.error("Unrecognized event payload in matching engine event with id {}", event.getRequestId(), ex);
            return null;
        }
    }


}
