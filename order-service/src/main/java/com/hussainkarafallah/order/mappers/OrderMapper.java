package com.hussainkarafallah.order.mappers;

import com.hussainkarafallah.interfaces.FulfillmentSnapshot;
import com.hussainkarafallah.interfaces.OrderSnapshot;
import com.hussainkarafallah.order.domain.Fulfillment;
import com.hussainkarafallah.order.domain.Order;

import lombok.NonNull;

public class OrderMapper {

    public static OrderSnapshot toOrderSnapshot(@NonNull Order order) {
        return OrderSnapshot.builder()
                .id(order.getId())
                .instrument(order.getInstrument().name())
                .state(order.getState().name())
                .type(order.getOrderType().name())
                .fulfillments(order.getFulfillments().stream().map(OrderMapper::toFulfillmentSnapshot).toList())
                .traderId(order.getTraderId())
                .build();
    }

    public static FulfillmentSnapshot toFulfillmentSnapshot(@NonNull Fulfillment fulfillment) {
        return FulfillmentSnapshot.builder()
                .id(fulfillment.getId())
                .instrument(fulfillment.getInstrument().name())
                .state(fulfillment.getState().name())
                .targetQuantity(fulfillment.getTargetQuantity())
                .fulfilledQuantity(fulfillment.getFulfilledQuantity())
                .targetPrice(fulfillment.getTargetPrice())
                .fulfilledPrice(fulfillment.getTargetPrice())
                .build();
    }

}
