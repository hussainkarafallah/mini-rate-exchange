package com.hussainkarafallah.order.infrastructure;


 import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.springframework.stereotype.Service;
    
@Service
public class IdempotenceService<S> {
    private final Map<UUID, S> executed = new ConcurrentHashMap<>();

    public S execute(UUID idempotenceKey, Supplier<S> runnable) {
        S result = executed.get(idempotenceKey);
        if (result != null) {
            return result;
        }
        result = executed.computeIfAbsent(idempotenceKey, k -> runnable.get());
        return result;
    }
}
