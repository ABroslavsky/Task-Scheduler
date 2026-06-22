package com.scheduler.decorator;

import com.scheduler.tasks.Task;

public interface TaskDecorator {

    Task decorate(Task task);
}
