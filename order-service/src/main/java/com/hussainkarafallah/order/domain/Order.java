package com.hussainkarafallah.order.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.hussainkarafallah.domain.FulfillmentState;
import com.hussainkarafallah.domain.Instrument;
import com.hussainkarafallah.domain.OrderState;
import com.hussainkarafallah.domain.OrderType;
import com.hussainkarafallah.order.DomainValidationException;
import com.hussainkarafallah.utils.UuidUtils;

import jakarta.annotation.Nonnull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@AllArgsConstructor
public class Order {

    @NonNull
    private final UUID id;

    @NonNull
    private final Instrument instrument;

    @NonNull
    private OrderState state;

    @NonNull
    private final OrderType orderType;

    @Nonnull
    private final Long traderId;

    @Nonnull
    List<Fulfillment> fulfillments;

    @Nonnull
    private final Instant dateCreated;

    @Nonnull
    private Instant dateUpdated;

    @Setter
    private Integer Version = 0;

    @Builder(builderMethodName = "newOrderBuilder")
    private Order(
        UUID id,
        Instrument instrument,
        OrderType orderType,
        BigDecimal price,
        BigDecimal targetQuantity,
        Long traderId,
        Function<Instrument,BigDecimal> priceSupplier
    ) {

        if (targetQuantity.compareTo(BigDecimal.ZERO) == -1) {
            throw new DomainValidationException("Target quantity cannot be negative");
        }
        if (price != null  && price.compareTo(BigDecimal.ZERO) == -1) {
            throw new DomainValidationException("Price cannot be negative");
        }

        this.id = id;
        this.instrument = instrument;
        this.state = OrderState.OPEN;
        this.orderType = orderType;
        this.traderId = traderId;
        this.dateCreated = Instant.now();
        this.dateUpdated = this.dateCreated;
        this.fulfillments = new ArrayList<>();
        
        if (instrument.isComposite()) {
            Map<Instrument , BigDecimal> componentsPrices = instrument.getComponents().stream().collect(
                Collectors.toMap(Function.identity(), priceSupplier::apply)
            );
            BigDecimal compositePrice = componentsPrices.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            instrument.getComponents().stream()
                .map(component -> Fulfillment.newFulfillment(
                    UuidUtils.generatePrefixCombUuid(),
                    component,
                    targetQuantity,
                    price.multiply(componentsPrices.get(component)).divide(compositePrice))
                )
                .forEach(this.fulfillments::add);
            
        } else {
            this.fulfillments.add(Fulfillment.newFulfillment(UuidUtils.generatePrefixCombUuid(), instrument, targetQuantity, price));
        }

        validate();
    }

    void validate() {
        this.fulfillments.forEach(Fulfillment::validate);
    }

    
    public void setState(OrderState newState){
        this.state = newState;
        validate();
    }

    public void setFullfilments(List<Fulfillment>fulfillments){
        this.fulfillments = fulfillments;
        validate();
    }

    public void addFulfillment(Fulfillment fulfillment){
        this.fulfillments.add(fulfillment);
    }

    public boolean canExecute() {
        boolean hasPending = fulfillments.stream().anyMatch(x -> x.getState().equals(FulfillmentState.NOT_COMPLETED));
        return !(hasPending && getInstrument().isComposite());
    };

    public boolean canBeClosed(){
        return fulfillments.stream().allMatch(
            f -> f.getState().equals(FulfillmentState.EXECUTED) || f.getState().equals(FulfillmentState.REVERSED)
        );
    }


}