package com.hussainkarafallah;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.hussainkarafallah.domain.Instrument;
import com.hussainkarafallah.domain.OrderType;
import com.hussainkarafallah.interfaces.OrderUpdateEvent;
import com.hussainkarafallah.interfaces.RequestMatchingEvent;
import com.hussainkarafallah.order.domain.Order;
import com.hussainkarafallah.order.domain.PriceBookEntry;
import com.hussainkarafallah.order.service.commands.CreateOrderCommand;

class CreateOrderTest extends BaseIntTest{

	@Test
	void createOrderTest() {
		// given
		var createOrderCommand = CreateOrderCommand.builder()
			.idempotencyUuid(UUID.randomUUID())
			.instrument(Instrument.IRNMDN)
			.orderType(OrderType.BUY)
			.traderId(123939L)
			.targetQuantity(aDecimal(304))
			.price(Optional.of(aDecimal(12)))
			.build();
		// when order is created
		Order created = createOrder.exec(createOrderCommand);
		// order is saved correctly in db
		Order fetchedBack = orderRepository.findById(created.getId()).orElseThrow();
		assertEquals(OrderType.BUY, fetchedBack.getOrderType());
		assertEquals(123939L, fetchedBack.getTraderId());
		// order update event is published
		awaitMessageSent(
			OrderUpdateEvent.class, 
			ev -> {
				return ev.getNewState().equals("OPEN")
				&& ev.getSnapshot().getTraderId() == 123939L;
			}
		);
		// matching is requested
		awaitMessageSent(
			RequestMatchingEvent.class,
			ev-> {
				return ev.getInstrument().equals("IRNMDN") &&
				ev.getQuantity().equals(aDecimal(304)) &&
				ev.getPrice().equals(aDecimal(12));
			}
		);
	}

	@Test
	void createCompositeOrder(){
		// given
		priceBook.save(new PriceBookEntry(Instrument.MTLCA, aDecimal(50)));
		priceBook.save(new PriceBookEntry(Instrument.MGDTH, aDecimal(100)));
		var createOrderCommand = CreateOrderCommand.builder()
			.idempotencyUuid(UUID.randomUUID())
			.instrument(Instrument.THRSH_MTL)
			.orderType(OrderType.BUY)
			.traderId(123939L)
			.targetQuantity(aDecimal(100))
			.price(Optional.of(aDecimal(270)))
			.build();
		// when
		// when order is created
		Order created = createOrder.exec(createOrderCommand);
		// order is saved correctly in db
		Order fetchedBack = orderRepository.findById(created.getId()).orElseThrow();
		assertEquals(Instrument.MGDTH , fetchedBack.getFulfillments().get(1).getInstrument());
		assertEquals(aDecimal(100) , fetchedBack.getFulfillments().get(1).getTargetQuantity());
		assertEquals(aDecimal(180) , fetchedBack.getFulfillments().get(1).getTargetPrice());
		assertEquals(Instrument.MTLCA , fetchedBack.getFulfillments().get(0).getInstrument());
		assertEquals(aDecimal(100) , fetchedBack.getFulfillments().get(0).getTargetQuantity());
		assertEquals(aDecimal(90) , fetchedBack.getFulfillments().get(0).getTargetPrice());
		// matching is requested
		awaitMessageSent(
			RequestMatchingEvent.class,
			ev-> {
				return ev.getInstrument().equals("MGDTH") &&
				ev.getQuantity().equals(aDecimal(100)) &&
				ev.getPrice().equals(aDecimal(180));
			}
		);
		awaitMessageSent(
			RequestMatchingEvent.class,
			ev-> {
				return ev.getInstrument().equals("MTLCA") &&
				ev.getQuantity().equals(aDecimal(100)) &&
				ev.getPrice().equals(aDecimal(90));
			}
		);


	}

}
