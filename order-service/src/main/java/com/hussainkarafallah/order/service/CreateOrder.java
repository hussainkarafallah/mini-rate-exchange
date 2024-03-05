package com.hussainkarafallah.order.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.hussainkarafallah.domain.Instrument;
import com.hussainkarafallah.order.domain.CompositeOrder;
import com.hussainkarafallah.order.domain.StockOrder;
import com.hussainkarafallah.order.infrastructure.IdempotenceService;
import com.hussainkarafallah.order.repository.OrderRepository;
import com.hussainkarafallah.order.repository.PriceBook;
import com.hussainkarafallah.order.service.commands.CreateOrderCommand;
import com.hussainkarafallah.utils.UuidUtils;

import lombok.RequiredArgsConstructor;;

@Service
@RequiredArgsConstructor
public class CreateOrder {

    private final IdempotenceService<UUID> idempotenceService;

    private final OrderRepository orderRepository;

    private final PublishOrderUpdate broadcastOrder;

    private final RequestFulfillment requestFulfillment;

    private final PriceBook priceBook;

    public UUID exec(CreateOrderCommand command) {
        // since prefixCombUUid will be unique rest of parameters are not critical but
        // just respecting library contract
        return idempotenceService.execute(
                command.getIdempotencyUuid(),
                () -> {
                    if (command.getInstrument().isComposite()) {
                        return createCompositeOrder(command).getId();
                    } else {
                        return createStockOrder(command, null).getId();
                    }
                });
    }

    private CompositeOrder createCompositeOrder(CreateOrderCommand command) {
        Instrument instrument = command.getInstrument();
        Map<Instrument, BigDecimal> componentsPrices = instrument.getComponents().stream().collect(
                Collectors.toMap(Function.identity(), i -> priceBook.findByInstrument(i).getPrice()));

        BigDecimal compositePrice = componentsPrices.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        UUID orderId = UuidUtils.generatePrefixCombUuid();
        List<UUID> stockOrdersIds = new ArrayList<>();
        for (Instrument component : instrument.getComponents()) {
            var componentPrice = componentsPrices.get(component).multiply(command.getPrice().orElseThrow()).divide(compositePrice);
            CreateOrderCommand childCommand = CreateOrderCommand.builder()
                .idempotencyUuid(UUID.randomUUID())
                .instrument(component)
                .orderType(command.getOrderType())
                .traderId(command.getTraderId())
                .targetQuantity(command.getTargetQuantity())
                .price(Optional.ofNullable(componentPrice))
                .build();
            StockOrder stockOrder = createStockOrder(childCommand, orderId);
            stockOrdersIds.add(stockOrder.getId());
        }
        CompositeOrder compositeOrder = new CompositeOrder(orderId, instrument, stockOrdersIds, Instant.now());
        orderRepository.register(compositeOrder);
        return compositeOrder;
    }

    private StockOrder createStockOrder(CreateOrderCommand command, UUID basketId){
        StockOrder order = StockOrder.newOrderBuilder()
            .id(UuidUtils.generatePrefixCombUuid())
            .instrument(command.getInstrument())
            .type(command.getOrderType())
            .targetQuantity(command.getTargetQuantity())
            .targetPrice(command.getPrice().orElse(null))
            .traderId(command.getTraderId())
            .basketId(basketId)
            .build();
        orderRepository.save(order);
        requestFulfillment.exec(order);
        broadcastOrder.onOrderCreated(order);
        return order;
    }

}
