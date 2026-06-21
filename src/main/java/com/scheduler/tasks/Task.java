package com.scheduler.tasks;

public interface Task {

    String getId();

    TaskMetadata getMetadata();

    void execute() throws Exception;
}
