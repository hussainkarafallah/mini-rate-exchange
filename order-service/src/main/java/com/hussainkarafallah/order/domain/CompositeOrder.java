package com.hussainkarafallah.order.domain;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.hussainkarafallah.domain.Instrument;

import jakarta.annotation.Nonnull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor
public class CompositeOrder {
    @NonNull
    private final UUID id;

    @NonNull
    private final Instrument instrument;

    @NonNull
    List<UUID> stockOrdersIds;

    @Nonnull
    private final Instant dateCreated;

}
