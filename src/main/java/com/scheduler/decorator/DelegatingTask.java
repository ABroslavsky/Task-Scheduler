package com.scheduler.decorator;

import com.scheduler.tasks.Task;
import com.scheduler.tasks.TaskMetadata;

import java.util.Objects;

abstract class DelegatingTask implements Task {

    private final Task delegate;

    DelegatingTask(Task delegate) {
        this.delegate = Objects.requireNonNull(delegate);
    }

    protected Task delegate() {
        return delegate;
    }

    @Override
    public String getId() {
        return delegate.getId();
    }

    @Override
    public TaskMetadata getMetadata() {
        return delegate.getMetadata();
    }
}
