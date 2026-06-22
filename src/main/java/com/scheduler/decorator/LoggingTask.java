package com.scheduler.decorator;

import com.scheduler.tasks.Task;

import java.util.logging.Level;
import java.util.logging.Logger;

final class LoggingTask extends DelegatingTask {

    private static final Logger LOGGER = Logger.getLogger(LoggingTask.class.getName());

    LoggingTask(Task delegate) {
        super(delegate);
    }

    @Override
    public void execute() throws Exception {
        LOGGER.log(Level.INFO, "Executing task {0} [{1}]",
                new Object[]{getMetadata().getName(), getId()});
        try {
            delegate().execute();
            LOGGER.log(Level.INFO, "Task {0} completed", getMetadata().getName());
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Task {0} failed: {1}",
                    new Object[]{getMetadata().getName(), ex.getMessage()});
            throw ex;
        }
    }
}
