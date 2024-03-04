package com.hussainkarafallah.interfaces;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.hussainkarafallah.domain.Instrument;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class TradeEvent {
    UUID id;
    Instrument instrument;
    Long firstTraderId;
    Long secondTraderId;
    BigDecimal price;
    Instant date;

}
