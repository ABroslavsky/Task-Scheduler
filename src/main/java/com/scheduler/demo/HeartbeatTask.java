package com.scheduler.demo;

import com.scheduler.tasks.AbstractTask;
import com.scheduler.tasks.TaskMetadata;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public final class HeartbeatTask extends AbstractTask {

    public static final String TASK_TYPE = "heartbeat";

    private final Path logFile;
    private final AtomicInteger runs = new AtomicInteger();

    public HeartbeatTask(String name, Path logFile) {
        super(TaskMetadata.builder()
                .name(name)
                .group("demo")
                .attribute("taskType", TASK_TYPE)
                .build());
        this.logFile = Objects.requireNonNull(logFile);
    }

    @Override
    public void execute() throws Exception {
        runs.incrementAndGet();
        if (logFile.getParent() != null) {
            Files.createDirectories(logFile.getParent());
        }
        String line = getMetadata().getName() + " " + Instant.now() + System.lineSeparator();
        Files.writeString(logFile, line, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    public int runs() {
        return runs.get();
    }

    public Path logFile() {
        return logFile;
    }
}
