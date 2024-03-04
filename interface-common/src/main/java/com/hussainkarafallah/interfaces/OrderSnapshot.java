package com.hussainkarafallah.interfaces;

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

    private List<FulfillmentSnapshot> fulfillments;

    private Long traderId;
}