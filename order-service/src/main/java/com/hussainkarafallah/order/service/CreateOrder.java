package com.hussainkarafallah.order.service;

import org.springframework.stereotype.Service;

import com.hussainkarafallah.order.domain.Order;
import com.hussainkarafallah.order.infrastructure.IdempotenceService;
import com.hussainkarafallah.order.repository.OrderRepository;
import com.hussainkarafallah.order.repository.PriceBook;
import com.hussainkarafallah.order.service.commands.CreateOrderCommand;
import com.hussainkarafallah.order.service.commands.RequestFulfillmentCommand;
import com.hussainkarafallah.utils.UuidUtils;

import lombok.RequiredArgsConstructor;;


@Service
@RequiredArgsConstructor
public class CreateOrder {

    private final IdempotenceService<Order> idempotenceService;

    private final OrderRepository orderRepository;

    private final PublishOrderUpdate broadcastOrder;

    private final RequestFulfillment requestFulfillment;

    private final PriceBook priceBook;

    public Order exec(CreateOrderCommand command){
        // since prefixCombUUid will be unique rest of parameters are not critical but just respecting library contract
        return idempotenceService.execute(
            command.getIdempotencyUuid(),
            () -> createOrder(command)
        );
    }

    private Order createOrder(CreateOrderCommand command){
        Order order = Order.newOrderBuilder()
            .id(UuidUtils.generatePrefixCombUuid())
            .instrument(command.getInstrument())
            .orderType(command.getOrderType())
            .targetQuantity(command.getTargetQuantity())
            .price(command.getPrice().orElse(null))
            .traderId(command.getTraderId())
            .priceSupplier(instrument -> priceBook.findByInstrument(instrument).getPrice())
            .build();
        orderRepository.save(order);
        order.getFulfillments().forEach(fulfillment -> {
            requestFulfillment.exec(new RequestFulfillmentCommand(order.getId(), order.getOrderType(), fulfillment));
        });
        broadcastOrder.onOrderCreated(order);
        return order;
    }

}
