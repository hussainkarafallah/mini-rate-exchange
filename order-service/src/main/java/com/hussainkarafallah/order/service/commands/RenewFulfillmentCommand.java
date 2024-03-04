package com.hussainkarafallah.order.service.commands;

import java.math.BigDecimal;
import java.util.UUID;

import com.hussainkarafallah.domain.Instrument;

import lombok.Value;

@Value
public class RenewFulfillmentCommand {
    UUID orderId;
    Instrument instrument;
    BigDecimal quantity;
    BigDecimal price;
}
