package com.hussainkarafallah.order.mappers;

import com.hussainkarafallah.interfaces.FulfillmentSnapshot;
import com.hussainkarafallah.interfaces.OrderSnapshot;
import com.hussainkarafallah.order.domain.Fulfillment;
import com.hussainkarafallah.order.domain.StockOrder;

import lombok.NonNull;

public class OrderMapper {

    public static OrderSnapshot toOrderSnapshot(@NonNull StockOrder order) {
        return OrderSnapshot.builder()
                .id(order.getId())
                .instrument(order.getInstrument().name())
                .state(order.getState().name())
                .type(order.getType().name())
                .fulfillments(order.getFulfillments().stream().map(OrderMapper::toFulfillmentSnapshot).toList())
                .traderId(order.getTraderId())
                .targetPrice(order.getTargetPrice())
                .targetQuantity(order.getTargetQuantity())
                .dateUpdated(order.getDateUpdated())
                .build();
    }

    public static FulfillmentSnapshot toFulfillmentSnapshot(@NonNull Fulfillment fulfillment) {
        return FulfillmentSnapshot.builder()
                .id(fulfillment.getId())
                .state(fulfillment.getState().name())
                .fulfilledQuantity(fulfillment.getFulfilledQuantity())
                .build();
    }

}
