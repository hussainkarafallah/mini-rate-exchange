package com.hussainkarafallah.order.service.commands;

import java.util.UUID;

import com.hussainkarafallah.domain.OrderType;
import com.hussainkarafallah.order.domain.Fulfillment;

import lombok.Value;

@Value
public class RequestFulfillmentCommand {
    UUID orderId;
    OrderType type;
    Fulfillment fulfillment;
}
