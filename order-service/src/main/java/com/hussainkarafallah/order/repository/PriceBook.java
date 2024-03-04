package com.hussainkarafallah.order.repository;

import java.util.List;

import com.hussainkarafallah.domain.Instrument;
import com.hussainkarafallah.order.domain.PriceBookEntry;

public interface PriceBook{
    PriceBookEntry findByInstrument(Instrument instrument);
    void save(PriceBookEntry entry);
    List<PriceBookEntry> getAllEntries();
}