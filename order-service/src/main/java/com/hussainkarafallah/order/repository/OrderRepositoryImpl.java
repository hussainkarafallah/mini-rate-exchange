package com.hussainkarafallah.order.repository;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Repository;

import com.hussainkarafallah.order.domain.Order;

@Repository
public class OrderRepositoryImpl implements OrderRepository {

    Map<UUID , Order> storage = new ConcurrentHashMap<>();

    @Override
    public Optional<Order> findById(UUID id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public void save(Order order) {
        storage.compute(
            order.getId(),
            (id , current) -> {
                if(current == null){
                    return order;
                }
                if(current.getVersion() != order.getVersion()){
                    throw new OptimisticLockingFailureException("concurrency issue");
                }
                order.setVersion(order.getVersion() + 1);
                return order;
            }
        );
    }

}
