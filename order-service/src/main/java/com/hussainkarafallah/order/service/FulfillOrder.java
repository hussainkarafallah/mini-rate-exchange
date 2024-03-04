package com.hussainkarafallah.order.service;

import org.springframework.stereotype.Service;

import com.hussainkarafallah.domain.FulfillmentState;
import com.hussainkarafallah.interfaces.OrderSnapshot;
import com.hussainkarafallah.order.domain.Fulfillment;
import com.hussainkarafallah.order.domain.Order;
import com.hussainkarafallah.order.mappers.OrderMapper;
import com.hussainkarafallah.order.repository.OrderRepository;
import com.hussainkarafallah.order.service.commands.FulfillOrderCommand;
import com.hussainkarafallah.order.service.commands.RequestFulfillmentCommand;
import com.hussainkarafallah.utils.UuidUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class FulfillOrder {

    private final OrderRepository orderRepository;

    private final PublishOrderUpdate publishOrderUpdate;

    private final RequestFulfillment requestFulfillment;

    

    public void exec(FulfillOrderCommand command){
        
        Order order = command.getOrder();
        Fulfillment fulfillment = command.getFulfillment();
        log.info("fulfilling order {} with {} of {} at {}", order.getId(), command.getQuantity(), order.getInstrument(), command.getPrice());


        OrderSnapshot beforeFulfullSnapshot = OrderMapper.toOrderSnapshot(order);

        fulfillment.setState(FulfillmentState.FULFILLED);
        fulfillment.setFulfilledPrice(command.getPrice());
        fulfillment.setFulfilledQuantity(command.getQuantity());
        fulfillment.setFulfullerId(command.getFulfillerId());

        if(fulfillment.getTargetQuantity().compareTo(command.getQuantity()) == 1){
            var newFulfillment = Fulfillment.newFulfillment(
                UuidUtils.generatePrefixCombUuid(),
                fulfillment.getInstrument(),
                fulfillment.getTargetQuantity().subtract(command.getQuantity()),
                fulfillment.getTargetPrice()
            );
            order.addFulfillment(newFulfillment);
            requestFulfillment.exec(new RequestFulfillmentCommand(order.getId(), order.getOrderType(), newFulfillment));
        }

        orderRepository.save(order);
        publishOrderUpdate.onOrderUpdated(beforeFulfullSnapshot, OrderMapper.toOrderSnapshot(order));
    }
}
