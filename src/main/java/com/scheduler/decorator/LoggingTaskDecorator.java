package com.scheduler.decorator;

import com.scheduler.tasks.Task;

public final class LoggingTaskDecorator implements TaskDecorator {

    @Override
    public Task decorate(Task task) {
        return new LoggingTask(task);
    }
}
