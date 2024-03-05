package com.hussainkarafallah.interfaces;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class OrderSnapshot {

    private UUID id;

    private String instrument;

    private String state;

    private String type;

    private BigDecimal targetQuantity;

    private BigDecimal targetPrice;

    private List<FulfillmentSnapshot> fulfillments;

    private Long traderId;

    private Instant dateUpdated;
}