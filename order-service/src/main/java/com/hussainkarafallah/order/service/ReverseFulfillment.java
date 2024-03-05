package com.hussainkarafallah.order.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.hussainkarafallah.interfaces.OrderSnapshot;
import com.hussainkarafallah.order.domain.Fulfillment;
import com.hussainkarafallah.order.domain.StockOrder;
import com.hussainkarafallah.order.mappers.OrderMapper;
import com.hussainkarafallah.order.repository.OrderRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReverseFulfillment {

    OrderRepository orderRepository;
    PublishOrderUpdate publishOrderUpdate;
    
    public void exec(StockOrder order, UUID fid){
        Fulfillment fulfillment = order.getFulfillments().stream().filter(f -> f.getId().equals(fid)).findAny().orElseThrow();
        exec(order, fulfillment);
    }
    public void exec(StockOrder order, Fulfillment fulfillment){
        OrderSnapshot beforeCancellationSnapshot = OrderMapper.toOrderSnapshot(order);
        fulfillment.close();
        orderRepository.save(order);
        publishOrderUpdate.onOrderUpdated(beforeCancellationSnapshot, OrderMapper.toOrderSnapshot(order));
    }
}
