package com.scheduler.execution;

import com.scheduler.core.SchedulerConfig;
import com.scheduler.events.EventBus;
import com.scheduler.events.TaskFailedEvent;

import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public final class ThreadPoolTaskExecutor implements TaskExecutor {

    private final ThreadPoolExecutor executor;
    private final EventBus eventBus;

    public ThreadPoolTaskExecutor(SchedulerConfig config, EventBus eventBus) {
        this.eventBus = Objects.requireNonNull(eventBus);
        this.executor = new ThreadPoolExecutor(
                config.getCorePoolSize(),
                config.getMaxPoolSize(),
                config.getKeepAliveSeconds(),
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(config.getQueueCapacity()),
                new WorkerThreadFactory(),
                (runnable, pool) -> handleRejection(runnable)
        );
    }

    @Override
    public Future<?> submit(Runnable command) {
        return executor.submit(command);
    }

    @Override
    public void shutdown() {
        executor.shutdown();
    }

    @Override
    public void shutdownNow() {
        executor.shutdownNow();
    }

    public void awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        executor.awaitTermination(timeout, unit);
    }

    private void handleRejection(Runnable runnable) {
        if (runnable instanceof RejectionAwareCommand rejectionAware) {
            eventBus.publish(new TaskFailedEvent(
                    rejectionAware.getJobId(),
                    rejectionAware.getTaskId(),
                    new IllegalStateException("Task rejected: thread pool queue is full"),
                    rejectionAware.getAttempt(),
                    Instant.now()
            ));
        }
    }

    private static final class WorkerThreadFactory implements ThreadFactory {

        private final AtomicInteger counter = new AtomicInteger();

        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable, "scheduler-worker-" + counter.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        }
    }
}
