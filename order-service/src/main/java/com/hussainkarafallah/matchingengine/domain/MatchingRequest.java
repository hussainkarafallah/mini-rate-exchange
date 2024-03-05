package com.hussainkarafallah.matchingengine.domain;

import java.math.BigDecimal;
import java.util.UUID;

import com.hussainkarafallah.domain.Instrument;
import com.hussainkarafallah.domain.MatchingType;

import io.micrometer.common.lang.NonNull;

import lombok.Builder;
import lombok.Value;


@Value
@Builder
public class MatchingRequest {
    @NonNull
    private UUID requestId;
    @NonNull
    private UUID orderId;
    @NonNull
    private Instrument instrument;
    private BigDecimal price;
    @NonNull
    private BigDecimal quantity;
    @NonNull
    private MatchingType type;
    private boolean partial;
}