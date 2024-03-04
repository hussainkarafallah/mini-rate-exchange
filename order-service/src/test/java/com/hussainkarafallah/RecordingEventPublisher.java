package com.hussainkarafallah;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.springframework.boot.test.context.TestComponent;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.hussainkarafallah.interfaces.FulfillmentMatchedEvent;
import com.hussainkarafallah.interfaces.OrderUpdateEvent;
import com.hussainkarafallah.interfaces.RequestMatchingEvent;
import com.hussainkarafallah.interfaces.TradeEvent;
import com.hussainkarafallah.order.infrastructure.EventPublisherImpl;

@Primary
@TestComponent
@Service
public class RecordingEventPublisher extends EventPublisherImpl {

    private final List<Object> recordedEvents;

    public RecordingEventPublisher() {
        super();
        this.recordedEvents = new ArrayList<>();
    }

    @Override
    public void publish(FulfillmentMatchedEvent event) {
        recordEvent(event);
        super.publish(event);
    }

    @Override
    public void publish(RequestMatchingEvent event) {
        recordEvent(event);
        super.publish(event);
    }

    @Override
    public void publish(TradeEvent event) {
        recordEvent(event);
        super.publish(event);
    }

    @Override
    public void publish(OrderUpdateEvent event) {
        recordEvent(event);
        super.publish(event);
    }

    private void recordEvent(Object event) {
        recordedEvents.add(event);
    }

    <T> Optional<T> findMessage(Class<T>clazz , Predicate<T>predicate){
        return recordedEvents.stream()
            .filter(clazz::isInstance)
            .map(clazz::cast)
            .filter(predicate::test)
            .findAny();
    }
}