package com.hussainkarafallah.order.service.commands;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import com.hussainkarafallah.domain.Instrument;
import com.hussainkarafallah.domain.OrderType;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CreateOrderCommand {
    private final UUID idempotencyUuid;
    private final Instrument instrument;
    private final OrderType orderType;
    private final BigDecimal targetQuantity;
    private final Optional<BigDecimal> price;
    private final Long traderId;
}
