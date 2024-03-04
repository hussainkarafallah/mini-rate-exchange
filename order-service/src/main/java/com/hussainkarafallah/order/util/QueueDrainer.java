package com.hussainkarafallah.order.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.function.Consumer;

public class QueueDrainer {
    public static <T> Runnable queueDrainer(BlockingQueue<T> queue , Consumer<T> consumer){
        return () -> {
            while(true){
                List<T> arr = new ArrayList<>();
                queue.drainTo(arr);
                arr.forEach(consumer::accept);
            }
        };
    }
}
