package com.scheduler.tasks;

import java.util.Objects;

public class RunnableTask extends AbstractTask {

    private final Runnable action;

    public RunnableTask(String name, Runnable action) {
        super(TaskMetadata.builder()
                .name(name)
                .attribute("taskType", name)
                .build());
        this.action = Objects.requireNonNull(action);
    }

    public RunnableTask(String id, TaskMetadata metadata, Runnable action) {
        super(id, metadata);
        this.action = Objects.requireNonNull(action);
    }

    @Override
    public void execute() {
        action.run();
    }
}
