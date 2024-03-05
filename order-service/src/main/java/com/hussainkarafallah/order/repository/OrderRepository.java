package com.hussainkarafallah.order.repository;

import java.util.Optional;
import java.util.UUID;

import com.hussainkarafallah.order.domain.CompositeOrder;
import com.hussainkarafallah.order.domain.StockOrder;

public interface OrderRepository {
    Optional<StockOrder> findById(UUID id);

    void save(StockOrder order);

    void register(CompositeOrder CompositeOrder);

    Optional<CompositeOrder> findBasketById(UUID id);
}

