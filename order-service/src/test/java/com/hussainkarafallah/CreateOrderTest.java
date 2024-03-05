package com.hussainkarafallah;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.hussainkarafallah.domain.Instrument;
import com.hussainkarafallah.domain.OrderType;
import com.hussainkarafallah.interfaces.OrderUpdateEvent;
import com.hussainkarafallah.interfaces.RequestMatchingEvent;
import com.hussainkarafallah.order.domain.CompositeOrder;
import com.hussainkarafallah.order.domain.PriceBookEntry;
import com.hussainkarafallah.order.domain.StockOrder;
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
		StockOrder created = createOrder(createOrderCommand);
		// order is saved correctly in db
		StockOrder fetchedBack = orderRepository.findById(created.getId()).orElseThrow();
		assertEquals(OrderType.BUY, fetchedBack.getType());
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
		CompositeOrder created = createCompositeOrder(createOrderCommand);
		// order is saved correctly in db
		StockOrder fst = orderRepository.findById(created.getStockOrdersIds().get(0)).orElseThrow();
		StockOrder snd = orderRepository.findById(created.getStockOrdersIds().get(1)).orElseThrow();
		assertEquals(Instrument.MGDTH , snd.getInstrument());
		assertEquals(aDecimal(100) , snd.getTargetQuantity());
		assertEquals(180L , snd.getTargetPrice().longValue());
		assertEquals(Instrument.MTLCA , fst.getInstrument());
		assertEquals(aDecimal(100) , fst.getTargetQuantity());
		assertEquals(aDecimal(90) , fst.getTargetPrice());
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
