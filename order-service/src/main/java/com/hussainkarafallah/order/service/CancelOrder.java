package com.hussainkarafallah.order.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.hussainkarafallah.domain.FulfillmentState;
import com.hussainkarafallah.interfaces.OrderSnapshot;
import com.hussainkarafallah.order.domain.CompositeOrder;
import com.hussainkarafallah.order.domain.Fulfillment;
import com.hussainkarafallah.order.domain.StockOrder;
import com.hussainkarafallah.order.mappers.OrderMapper;
import com.hussainkarafallah.order.repository.OrderRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CancelOrder {
    
    private final OrderRepository orderRepository;

    private final ReverseFulfillment reverseFulfillment;

    private final PublishOrderUpdate publishOrderUpdate;
    
    void cancelOrder(UUID orderId){
        var maybeComposite = orderRepository.findBasketById(orderId);
        if(maybeComposite.isPresent()){
            CompositeOrder compositeOrder = maybeComposite.get();
            compositeOrder.getStockOrdersIds().forEach(this::cancelOrder);
        }
        var maybeStock = orderRepository.findById(orderId);
        if(maybeStock.isPresent()){
            cancelStockOrder(maybeStock.get());
        }
        
    }


    private void cancelStockOrder(StockOrder order){
        OrderSnapshot beforeCancellationSnapshot = OrderMapper.toOrderSnapshot(order);
        for(Fulfillment fulfillment : order.getFulfillments()){
            if(fulfillment.getState().equals(FulfillmentState.MATCHED)){
                reverseFulfillment.exec(
                    orderRepository.findById(fulfillment.getFulfillerId()).orElseThrow(),
                    fulfillment.getId()
                );
                fulfillment.close();
            }
        }
        order.close();
        orderRepository.save(order);
        publishOrderUpdate.onOrderUpdated(beforeCancellationSnapshot, OrderMapper.toOrderSnapshot(order));
        
    }
}
