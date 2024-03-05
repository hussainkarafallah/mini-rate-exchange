package com.hussainkarafallah.order.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.hussainkarafallah.domain.FulfillmentState;
import com.hussainkarafallah.domain.Instrument;
import com.hussainkarafallah.domain.OrderState;
import com.hussainkarafallah.domain.OrderType;
import com.hussainkarafallah.order.DomainValidationException;

import jakarta.annotation.Nonnull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@AllArgsConstructor
public class StockOrder {

    @NonNull
    private final UUID id;

    @NonNull
    private final Instrument instrument;

    @NonNull
    private OrderState state;

    @NonNull
    private final OrderType type;

    @Nonnull
    private final Long traderId;

    @Nonnull
    List<Fulfillment> fulfillments;

    @NonNull
    BigDecimal targetPrice;

    @NonNull
    BigDecimal targetQuantity;

    @Nonnull
    private final Instant dateCreated;

    @Nonnull
    private Instant dateUpdated;

    @Setter
    private Integer Version = 0;

    UUID basketId;

    @Builder(builderMethodName = "newOrderBuilder")
    private StockOrder(
            UUID id,
            Instrument instrument,
            OrderType type,
            BigDecimal targetPrice,
            BigDecimal targetQuantity,
            Long traderId,
            UUID basketId) {

        if (instrument.isComposite()) {
            throw new DomainValidationException("Stock order cannot be of composite instrument");
        }

        if (targetQuantity.compareTo(BigDecimal.ZERO) == -1) {
            throw new DomainValidationException("Target quantity cannot be negative");
        }
        if (targetPrice != null && targetPrice.compareTo(BigDecimal.ZERO) == -1) {
            throw new DomainValidationException("Price cannot be negative");
        }

        this.id = id;
        this.instrument = instrument;
        this.state = OrderState.OPEN;
        this.type = type;
        this.traderId = traderId;
        this.targetPrice = targetPrice;
        this.targetQuantity = targetQuantity;
        this.dateCreated = Instant.now();
        this.dateUpdated = this.dateCreated;
        this.fulfillments = new ArrayList<>();
        this.basketId = basketId;
        validate();
    }

    void validate() {
        this.fulfillments.forEach(Fulfillment::validate);
    }

    public void addFulfillment(Fulfillment fulfillment) {
        this.fulfillments.add(fulfillment);
        dateUpdated = Instant.now();
        validate();
    }

    public BigDecimal anticipatedFulfillment() {
        BigDecimal matched = fulfillments.stream()
                .filter(x -> !x.getState().equals(FulfillmentState.REVERSED))
                .map(x -> x.getFulfilledQuantity())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return targetQuantity.subtract(matched);
    }

    public boolean canBeClosed() {
        return anticipatedFulfillment().compareTo(BigDecimal.ZERO) == 0
                && fulfillments.stream().allMatch(
                    f -> f.getState().equals(FulfillmentState.EXECUTED)|| f.getState().equals(FulfillmentState.REVERSED));
    }

    public void close() {
        this.state = OrderState.CLOSED;
        this.fulfillments.forEach(Fulfillment::close);
        dateUpdated = Instant.now();
        validate();
    }

}