package com.hussainkarafallah.order.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hussainkarafallah.domain.FulfillmentState;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class Fulfillment {
    @NonNull
    private final UUID id;

    @NonNull
    private FulfillmentState state;

    @NonNull
    private UUID fulfillerId;

    @NonNull
    private BigDecimal fulfilledQuantity;

    @NonNull
    private BigDecimal fulfilledPrice;

    @NonNull
    private Instant dateUpdated;

    public void validate() {
        // fill when necessary
    }

    @JsonCreator
    @Builder
    public Fulfillment(
        @JsonProperty("id") UUID id,
        @JsonProperty("state") FulfillmentState state,
        @JsonProperty("fulfillerId") UUID fulfillerId,
        @JsonProperty("fulfilledQuantity") BigDecimal fulfilledQuantity,
        @JsonProperty("fulfilledPrice") BigDecimal fulfilledPrice,
        @JsonProperty("dateUpdated") Instant dateUpdated
    )  {
        this.id = id;
        this.state = state;
        this.fulfillerId = fulfillerId;
        this.fulfilledQuantity = fulfilledQuantity;
        this.fulfilledPrice = fulfilledPrice;
        this.dateUpdated = dateUpdated;
        validate();
    };


    public void setState(FulfillmentState state){
        this.state = state;
        validate();
    }

    public void setFulfilledPrice(BigDecimal price){
        this.fulfilledPrice = price;
        validate();
    }

    public void setFulfillerId(UUID id){
        this.fulfillerId = id;
        validate();
    }

    public void setFulfilledQuantity(BigDecimal quantity){
        this.fulfilledQuantity = quantity;
        validate();
    }

    public void close(){
        if(state == FulfillmentState.MATCHED){
            setState(FulfillmentState.REVERSED);
            validate();
        }
    }

}
