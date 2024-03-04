package com.hussainkarafallah.order.service;

import java.math.BigDecimal;
import java.time.Instant;

import org.springframework.stereotype.Service;

import com.hussainkarafallah.EventPublisher;
import com.hussainkarafallah.domain.Instrument;
import com.hussainkarafallah.interfaces.TradeEvent;
import com.hussainkarafallah.order.domain.PriceBookEntry;
import com.hussainkarafallah.order.repository.PriceBook;
import com.hussainkarafallah.utils.UuidUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExecuteTrade {
    private final PriceBook priceBook;

    private final EventPublisher eventPublisher;

    public void executeTrade(Instrument instrument, BigDecimal price, Long firstTraderId , Long secondTraderId){
        priceBook.save(new PriceBookEntry(instrument, price));
        TradeEvent event = TradeEvent.builder()
            .id(UuidUtils.generatePrefixCombUuid())
            .instrument(instrument)
            .price(price)
            .firstTraderId(firstTraderId)
            .secondTraderId(secondTraderId)
            .date(Instant.now())
            .build();
        eventPublisher.publish(event);
    }
}
