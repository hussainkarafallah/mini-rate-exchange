package com.hussainkarafallah.order.repository;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import com.hussainkarafallah.order.domain.CompositeOrder;
import com.hussainkarafallah.order.domain.StockOrder;

@Repository
public class OrderRepositoryImpl implements OrderRepository {

    Map<UUID , StockOrder> stockStorage = new ConcurrentHashMap<>();

    Map<UUID , CompositeOrder> compositeStorage = new ConcurrentHashMap<>();

    @Override
    public Optional<StockOrder> findById(UUID id) {
        return Optional.ofNullable(stockStorage.get(id));
    }

    @Override
    public void save(StockOrder order) {
        stockStorage.compute(
            order.getId(),
            (id , current) -> {
                if(current == null){
                    return order;
                }
                if(current.getVersion() != order.getVersion()){
                    throw new RuntimeException("concurrency optimistic locking issue");
                }
                order.setVersion(order.getVersion() + 1);
                return order;
            }
        );
    }

    @Override
    public void register(CompositeOrder compositeOrder) {
        compositeStorage.put(compositeOrder.getId(), compositeOrder);
    }

    @Override
    public Optional<CompositeOrder> findBasketById(UUID id) {
        return Optional.ofNullable(compositeStorage.get(id));
    }

}
