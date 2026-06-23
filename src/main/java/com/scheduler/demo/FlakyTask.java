package com.scheduler.demo;

import com.scheduler.tasks.AbstractTask;
import com.scheduler.tasks.TaskMetadata;

import java.util.concurrent.atomic.AtomicInteger;

public final class FlakyTask extends AbstractTask {

    public static final String TASK_TYPE = "flaky";

    private final AtomicInteger attempts = new AtomicInteger();

    public FlakyTask() {
        super(TaskMetadata.builder()
                .name("payment-retry")
                .group("demo")
                .attribute("taskType", TASK_TYPE)
                .build());
    }

    @Override
    public void execute() throws Exception {
        int attempt = attempts.incrementAndGet();
        if (attempt <= 2) {
            throw new IllegalStateException("upstream rejected request (attempt " + attempt + ")");
        }
    }
}
