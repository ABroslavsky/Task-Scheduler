package com.scheduler.decorator;

import com.scheduler.tasks.Task;

final class MetricsRecordingTask extends DelegatingTask {

    private final MetricsTaskDecorator metrics;

    MetricsRecordingTask(Task delegate, MetricsTaskDecorator metrics) {
        super(delegate);
        this.metrics = metrics;
    }

    @Override
    public void execute() throws Exception {
        MetricsTaskDecorator.TaskMetrics taskMetrics =
                metrics.metricsFor(getId());
        long start = System.nanoTime();
        taskMetrics.executions().incrementAndGet();
        try {
            delegate().execute();
        } catch (Exception ex) {
            taskMetrics.failures().incrementAndGet();
            throw ex;
        } finally {
            taskMetrics.totalDurationMs().addAndGet((System.nanoTime() - start) / 1_000_000L);
        }
    }
}
