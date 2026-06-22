package com.scheduler.events;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class EventBus {

    private final List<SchedulerEventListener> listeners = new CopyOnWriteArrayList<>();

    public void register(SchedulerEventListener listener) {
        listeners.add(listener);
    }

    public void unregister(SchedulerEventListener listener) {
        listeners.remove(listener);
    }

    public void publish(SchedulerEvent event) {
        for (SchedulerEventListener listener : listeners) {
            listener.onEvent(event);
        }
    }
}
