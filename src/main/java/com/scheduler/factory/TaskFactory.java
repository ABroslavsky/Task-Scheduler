package com.scheduler.factory;

import com.scheduler.tasks.Task;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public final class TaskFactory {

    private final Map<String, Supplier<Task>> registry = new ConcurrentHashMap<>();

    public void register(String taskType, Supplier<Task> supplier) {
        registry.put(Objects.requireNonNull(taskType), Objects.requireNonNull(supplier));
    }

    public Task create(String taskType) {
        Supplier<Task> supplier = registry.get(taskType);
        if (supplier == null) {
            throw new IllegalArgumentException("Unknown task type: " + taskType);
        }
        return supplier.get();
    }
}
