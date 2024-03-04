package com.hussainkarafallah.order.domain;

import java.math.BigDecimal;

import com.hussainkarafallah.domain.Instrument;

import lombok.Value;

@Value
public class PriceBookEntry {
    Instrument instrument;
    BigDecimal price;
}
