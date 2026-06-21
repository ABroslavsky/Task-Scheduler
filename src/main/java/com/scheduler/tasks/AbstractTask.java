package com.scheduler.tasks;

import java.util.UUID;

public abstract class AbstractTask implements Task {

    private final String id;
    private final TaskMetadata metadata;

    protected AbstractTask(TaskMetadata metadata) {
        this.id = UUID.randomUUID().toString();
        this.metadata = metadata;
    }

    protected AbstractTask(String id, TaskMetadata metadata) {
        this.id = id;
        this.metadata = metadata;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public TaskMetadata getMetadata() {
        return metadata;
    }
}
