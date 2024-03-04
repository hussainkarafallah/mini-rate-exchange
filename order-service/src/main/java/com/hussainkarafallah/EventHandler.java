package com.hussainkarafallah;

public interface EventHandler<T> {
    void onEvent(T event);
}
