package com.scheduler.execution;

public interface RejectionAwareCommand {

    String getJobId();

    String getTaskId();

    int getAttempt();
}
