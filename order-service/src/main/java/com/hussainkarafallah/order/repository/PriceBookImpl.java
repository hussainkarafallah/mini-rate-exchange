package com.hussainkarafallah.order.repository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import com.hussainkarafallah.domain.Instrument;
import com.hussainkarafallah.order.domain.PriceBookEntry;

@Repository
public class PriceBookImpl implements PriceBook {

    Map<Instrument, PriceBookEntry> storage = new ConcurrentHashMap<>();

    @Override
    public PriceBookEntry findByInstrument(Instrument instrument) {
        return storage.get(instrument);
    }

    @Override
    public void save(PriceBookEntry entry) {
        storage.put(entry.getInstrument(), entry);
    }

    @Override
    public List<PriceBookEntry> getAllEntries() {
        return storage.values().stream().toList();
    }

    

}
