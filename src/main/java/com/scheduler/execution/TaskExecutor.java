package com.scheduler.execution;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public interface TaskExecutor {

    Future<?> submit(Runnable command);

    void shutdown();

    void shutdownNow();

    void awaitTermination(long timeout, TimeUnit unit) throws InterruptedException;
}
