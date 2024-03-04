package com.hussainkarafallah.order.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hussainkarafallah.domain.FulfillmentState;
import com.hussainkarafallah.domain.Instrument;
import com.hussainkarafallah.order.DomainValidationException;

import lombok.Getter;
import lombok.NonNull;

@Getter
public class Fulfillment {
    @NonNull
    private final UUID id;

    @NonNull
    private final Instrument instrument;

    @NonNull
    private FulfillmentState state;

    @NonNull
    private final BigDecimal targetQuantity;

    @NonNull
    private final BigDecimal targetPrice;

    private UUID fulfillerId;
    private BigDecimal fulfilledQuantity;
    private BigDecimal fulfilledPrice;

    @NonNull
    private Instant dateUpdated;

    @JsonCreator
    public Fulfillment(
        @JsonProperty("id") UUID id,
        @JsonProperty("instrument") Instrument instrument,
        @JsonProperty("state") FulfillmentState state,
        @JsonProperty("targetQuantity") BigDecimal targetQuantity,
        @JsonProperty("targetPrice") BigDecimal targetPrice,
        @JsonProperty("fulfillerId") UUID fulfillerId,
        @JsonProperty("fulfilledQuantity") BigDecimal fulfilledQuantity,
        @JsonProperty("fulfilledPrice") BigDecimal fulfilledPrice,
        @JsonProperty("dateUpdated") Instant dateUpdated
    )  {
        this.id = id;
        this.instrument = instrument;
        this.state = state;
        this.targetQuantity = targetQuantity;
        this.targetPrice = targetPrice;
        this.fulfillerId = fulfillerId;
        this.fulfilledQuantity = fulfilledQuantity;
        this.fulfilledPrice = fulfilledPrice;
        this.dateUpdated = dateUpdated;
        if (this.instrument.isComposite()) {
            throw new DomainValidationException("fulfillments can only be for simple instruments");
        }
        if (state.equals(FulfillmentState.NOT_COMPLETED)) {
            if (fulfilledQuantity != BigDecimal.ZERO || fulfilledPrice != null || fulfillerId != null)  {
                throw new DomainValidationException("Idle fulfillments should have null matching price and quantity");
            }
        }
    };

    public static Fulfillment newFulfillment(UUID id, Instrument instrument, BigDecimal targetQuantity, BigDecimal targetPrice) {
        return new Fulfillment(
            id,
            instrument,
            FulfillmentState.NOT_COMPLETED,
            targetQuantity,
            targetPrice,
            null,
            BigDecimal.ZERO,
            null,
            Instant.now()
        );
    }

    public void validate() {
        // todo
    }

    public void setState(FulfillmentState state){
        this.state = state;
        validate();
    }

    public void setFulfilledPrice(BigDecimal price){
        this.fulfilledPrice = price;
        validate();
    }

    public void setFulfullerId(UUID id){
        this.fulfillerId = id;
        validate();
    }

    public void setFulfilledQuantity(BigDecimal quantity){
        this.fulfilledQuantity = quantity;
        validate();
    }

}
