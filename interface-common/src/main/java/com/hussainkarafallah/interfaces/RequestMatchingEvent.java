package com.hussainkarafallah.interfaces;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class RequestMatchingEvent {
    UUID requestId;
    UUID orderId;
    String instrument;
    BigDecimal price;
    BigDecimal quantity;
    String type;
}
