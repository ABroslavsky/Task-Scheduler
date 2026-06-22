package com.scheduler.decorator;

import com.scheduler.events.EventBus;
import com.scheduler.retry.RetryPolicy;
import com.scheduler.tasks.Task;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public final class MetricsTaskDecorator implements TaskDecorator {

    private final Map<String, TaskMetrics> metricsByTaskId = new ConcurrentHashMap<>();

    @Override
    public Task decorate(Task task) {
        return new MetricsRecordingTask(task, this);
    }

    TaskMetrics metricsFor(String taskId) {
        return metricsByTaskId.computeIfAbsent(taskId, ignored -> new TaskMetrics());
    }

    public TaskMetrics getMetrics(String taskId) {
        return metricsByTaskId.getOrDefault(taskId, new TaskMetrics());
    }

    public static final class TaskMetrics {

        private final AtomicLong executions = new AtomicLong();
        private final AtomicLong failures = new AtomicLong();
        private final AtomicLong totalDurationMs = new AtomicLong();

        AtomicLong executions() {
            return executions;
        }

        AtomicLong failures() {
            return failures;
        }

        AtomicLong totalDurationMs() {
            return totalDurationMs;
        }

        public long getExecutions() {
            return executions.get();
        }

        public long getFailures() {
            return failures.get();
        }

        public long getTotalDurationMs() {
            return totalDurationMs.get();
        }
    }
}
