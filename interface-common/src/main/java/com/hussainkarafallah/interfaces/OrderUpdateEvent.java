package com.hussainkarafallah.interfaces;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class OrderUpdateEvent {
    private UUID eventId;
    private UUID orderId;
    private String oldState;
    private String newState;
    private OrderSnapshot snapshot;
}
