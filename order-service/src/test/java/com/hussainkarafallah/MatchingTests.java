package com.hussainkarafallah;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.hussainkarafallah.domain.Instrument;
import com.hussainkarafallah.domain.OrderState;
import com.hussainkarafallah.domain.OrderType;
import com.hussainkarafallah.interfaces.FulfillmentMatchedEvent;
import com.hussainkarafallah.interfaces.RequestMatchingEvent;
import com.hussainkarafallah.order.domain.Order;
import com.hussainkarafallah.order.service.commands.CreateOrderCommand;

import lombok.SneakyThrows;

public class MatchingTests extends BaseIntTest {
    @Test
    void testSimpleMatching(){
        // given
		var cmd1 = CreateOrderCommand.builder()
			.idempotencyUuid(UUID.randomUUID())
			.instrument(Instrument.IRNMDN)
			.orderType(OrderType.BUY)
			.traderId(123939L)
			.targetQuantity(aDecimal(304))
			.price(Optional.of(aDecimal(100)))
			.build();
        var cmd2 = CreateOrderCommand.builder()
			.idempotencyUuid(UUID.randomUUID())
			.instrument(Instrument.IRNMDN)
			.orderType(OrderType.SELL)
			.traderId(823783L)
			.targetQuantity(aDecimal(500))
			.price(Optional.of(aDecimal(12)))
			.build();
        // when
        var order1 = createOrder.exec(cmd1);
        var order2 = createOrder.exec(cmd2);
        // then
        awaitMessageSent(
			RequestMatchingEvent.class,
			ev-> ev.getInstrument().equals("IRNMDN") &&
				ev.getType().equals("BUY")
		);
        awaitMessageSent(
			RequestMatchingEvent.class,
			ev->  ev.getInstrument().equals("IRNMDN") &&
				ev.getType().equals("SELL")
		);
        var match = awaitMessageSent(
            FulfillmentMatchedEvent.class,
            ev -> ev.getBuyOrderId().equals(order1.getId())
            && ev.getSellOrderId().equals(order2.getId())
        );
        assertEquals(aDecimal(12) , match.getPrice());
        assertEquals(aDecimal(304), match.getQuantity());
    }

    @Test
    void buyOrderWithNoPriceMatches(){
        // given
		var cmd1 = CreateOrderCommand.builder()
        .idempotencyUuid(UUID.randomUUID())
        .instrument(Instrument.MGDTH)
        .orderType(OrderType.BUY)
        .traderId(123939L)
        .targetQuantity(aDecimal(39))
        .price(Optional.empty())
        .build();
        var cmd2 = CreateOrderCommand.builder()
            .idempotencyUuid(UUID.randomUUID())
            .instrument(Instrument.MGDTH)
            .orderType(OrderType.SELL)
            .traderId(823783L)
            .targetQuantity(aDecimal(22))
            .price(Optional.of(aDecimal(1700)))
            .build();
        // when
        var order1 = createOrder.exec(cmd1);
        var order2 = createOrder.exec(cmd2);
        // then
        awaitMessageSent(
            RequestMatchingEvent.class,
            ev-> ev.getInstrument().equals("MGDTH") &&
                ev.getType().equals("BUY")
        );
        awaitMessageSent(
            RequestMatchingEvent.class,
            ev->  ev.getInstrument().equals("MGDTH") &&
                ev.getType().equals("SELL")
        );
        var match = awaitMessageSent(
            FulfillmentMatchedEvent.class,
            ev -> ev.getBuyOrderId().equals(order1.getId())
            && ev.getSellOrderId().equals(order2.getId())
        );
        assertEquals(aDecimal(1700) , match.getPrice());
        assertEquals(aDecimal(22), match.getQuantity());
    }


    @Test
    void sellOrderWithNoPriceMatches(){
        // given
		var cmd1 = CreateOrderCommand.builder()
            .idempotencyUuid(UUID.randomUUID())
            .instrument(Instrument.MTLCA)
            .orderType(OrderType.BUY)
            .traderId(123939L)
            .targetQuantity(aDecimal(304))
            .price(Optional.of(aDecimal(100)))
            .build();
        var cmd2 = CreateOrderCommand.builder()
            .idempotencyUuid(UUID.randomUUID())
            .instrument(Instrument.MTLCA)
            .orderType(OrderType.SELL)
            .traderId(823783L)
            .targetQuantity(aDecimal(500))
            .price(Optional.empty())
            .build();
        // when
        var order1 = createOrder.exec(cmd1);
        var order2 = createOrder.exec(cmd2);
        // then
        awaitMessageSent(
            RequestMatchingEvent.class,
            ev-> ev.getInstrument().equals("MTLCA") &&
                ev.getType().equals("BUY")
        );
        awaitMessageSent(
            RequestMatchingEvent.class,
            ev->  ev.getInstrument().equals("MTLCA") &&
                ev.getType().equals("SELL")
        );
        var match = awaitMessageSent(
            FulfillmentMatchedEvent.class,
            ev -> ev.getBuyOrderId().equals(order1.getId())
            && ev.getSellOrderId().equals(order2.getId())
        );
        assertEquals(aDecimal(100) , match.getPrice());
        assertEquals(aDecimal(304), match.getQuantity());
    }

    @Test
    @SneakyThrows
    void simpleMatchingAndTrading(){
        // given
		var cmd1 = CreateOrderCommand.builder()
            .idempotencyUuid(UUID.randomUUID())
            .instrument(Instrument.IRNMDN)
            .orderType(OrderType.SELL)
            .traderId(3827L)
            .targetQuantity(aDecimal(10))
            .price(Optional.of(aDecimal(100)))
            .build();
        var cmd2 = CreateOrderCommand.builder()
            .idempotencyUuid(UUID.randomUUID())
            .instrument(Instrument.IRNMDN)
            .orderType(OrderType.SELL)
            .traderId(2933L)
            .targetQuantity(aDecimal(8))
            .price(Optional.of(aDecimal(50)))
            .build();
        var cmd3 = CreateOrderCommand.builder()
            .idempotencyUuid(UUID.randomUUID())
            .instrument(Instrument.IRNMDN)
            .orderType(OrderType.BUY)
            .traderId(823783L)
            .targetQuantity(aDecimal(12))
            .price(Optional.of(aDecimal(1000)))
            .build();
        var cmd4 = CreateOrderCommand.builder()
            .idempotencyUuid(UUID.randomUUID())
            .instrument(Instrument.IRNMDN)
            .orderType(OrderType.BUY)
            .traderId(811783L)
            .targetQuantity(aDecimal(9))
            .price(Optional.of(aDecimal(500)))
            .build();
        // when
        Order order1 = createOrder.exec(cmd1);
        Order order2 = createOrder.exec(cmd2);
        Order order3 = createOrder.exec(cmd3);
        Order order4 = createOrder.exec(cmd4);
        // then
        var match1 = awaitMatchBetween(order3 , order2);
        assertEquals(aDecimal(8), match1.getQuantity());
        assertEquals(aDecimal(50), match1.getPrice());

        var match2 = awaitMatchBetween(order4 , order1);
        assertEquals(aDecimal(9), match2.getQuantity());
        assertEquals(aDecimal(100), match2.getPrice());


        Thread.sleep(60000);
        var match3 = awaitMatchBetween(order3 , order1);
        assertEquals(aDecimal(1), match3.getQuantity());
        assertEquals(aDecimal(100) , match3.getPrice());

        verifyOrderState(order1.getId() , OrderState.CLOSED);
        verifyOrderState(order2.getId() , OrderState.CLOSED);
        verifyOrderState(order3.getId() , OrderState.OPEN);
        verifyOrderState(order4.getId() , OrderState.CLOSED);


    
    }
}
