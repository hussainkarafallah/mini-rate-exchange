package com.hussainkarafallah.order.service.commands;

import java.math.BigDecimal;
import java.util.UUID;

import com.hussainkarafallah.order.domain.Fulfillment;
import com.hussainkarafallah.order.domain.Order;

import io.micrometer.common.lang.NonNull;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class FulfillOrderCommand {
    @NonNull
    Order order;
    @NonNull
    Fulfillment fulfillment;
    @NonNull
    UUID fulfillerId;
    @NonNull
    UUID matchId;
    @NonNull
    BigDecimal quantity;
    @NonNull
    BigDecimal price;
}
