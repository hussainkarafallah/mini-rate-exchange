package com.hussainkarafallah.order.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.hussainkarafallah.domain.FulfillmentState;
import com.hussainkarafallah.interfaces.OrderSnapshot;
import com.hussainkarafallah.order.domain.Fulfillment;
import com.hussainkarafallah.order.domain.Order;
import com.hussainkarafallah.order.mappers.OrderMapper;
import com.hussainkarafallah.order.repository.OrderRepository;
import com.hussainkarafallah.order.service.commands.RenewFulfillmentCommand;
import com.hussainkarafallah.order.service.commands.RequestFulfillmentCommand;
import com.hussainkarafallah.utils.UuidUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RenewFulfillment {

    private final OrderRepository orderRepository;

    private final RequestFulfillment requestFulfillment;

    private final PublishOrderUpdate publishOrderUpdate;

    public void exec(RenewFulfillmentCommand command){
        
        Order order = orderRepository.findById(command.getOrderId()).orElseThrow();
        List<Fulfillment> fulfillemnts = order.getFulfillments();
        Fulfillment fulfillment = fulfillemnts
            .stream()
            .filter(fulfillemnt -> fulfillemnt.getInstrument().equals(command.getInstrument()) && fulfillemnt.getState().equals(FulfillmentState.FULFILLED))
            .findAny()
            .orElseThrow(() -> new IllegalStateException("trying to reverse a fulfillment that is not found"));
        
        if(!(fulfillment.getFulfilledQuantity().equals(command.getQuantity()) && fulfillment.getFulfilledPrice().equals(command.getPrice()))){
            throw new IllegalStateException("Fulfillment inconsistency issue");
        }

        OrderSnapshot beforeRenewalSnapshot = OrderMapper.toOrderSnapshot(order);
        //
        Fulfillment renewedFulfillment = renewedFulfillment(fulfillment);
        fulfillment.setState(FulfillmentState.REVERSED);
        order.addFulfillment(renewedFulfillment);

        //
        requestFulfillment.exec(new RequestFulfillmentCommand(order.getId(), order.getOrderType(), renewedFulfillment));
        orderRepository.save(order);
        publishOrderUpdate.onOrderUpdated(beforeRenewalSnapshot, OrderMapper.toOrderSnapshot(order));
    }

    private final Fulfillment renewedFulfillment(Fulfillment oldFulfillment){
        // the new fulfillment id will be random (old uuid) + 1997
        // this way when the message reaches matching engine, the time priority will be still maintained
        return Fulfillment.newFulfillment(
            UuidUtils.add(oldFulfillment.getId(), 1997),
            oldFulfillment.getInstrument(),
            oldFulfillment.getTargetQuantity(),
            oldFulfillment.getTargetPrice()
        );
    }
}
