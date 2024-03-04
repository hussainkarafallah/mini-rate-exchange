package com.hussainkarafallah.order.repository;

import java.util.Optional;
import java.util.UUID;

import com.hussainkarafallah.order.domain.Order;

public interface OrderRepository {
    Optional<Order> findById(UUID id);

    void save(Order order);
}
