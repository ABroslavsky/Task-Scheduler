package com.scheduler.persistence;

import com.scheduler.core.JobState;
import com.scheduler.core.ScheduledJob;
import com.scheduler.core.TriggerContext;
import com.scheduler.factory.TaskFactory;
import com.scheduler.retry.MaxAttemptsRetryPolicy;
import com.scheduler.retry.RetryPolicy;
import com.scheduler.tasks.Task;
import com.scheduler.triggers.CronTrigger;
import com.scheduler.triggers.FixedDelayTrigger;
import com.scheduler.triggers.FixedRateTrigger;
import com.scheduler.triggers.Trigger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class FileJobRepository implements JobRepository {

    private final Path storageFile;
    private final TaskFactory taskFactory;
    private final Map<String, ScheduledJob> cache = new ConcurrentHashMap<>();

    public FileJobRepository(Path storageFile, TaskFactory taskFactory) {
        this.storageFile = Objects.requireNonNull(storageFile);
        this.taskFactory = Objects.requireNonNull(taskFactory);
        loadFromDisk();
    }

    @Override
    public void save(ScheduledJob job) {
        cache.put(job.getJobId(), job);
        flushToDisk();
    }

    @Override
    public Optional<ScheduledJob> findById(String jobId) {
        return Optional.ofNullable(cache.get(jobId));
    }

    @Override
    public Collection<ScheduledJob> findAll() {
        return List.copyOf(cache.values());
    }

    @Override
    public void delete(String jobId) {
        cache.remove(jobId);
        flushToDisk();
    }

    private void loadFromDisk() {
        if (!Files.exists(storageFile)) {
            return;
        }
        try {
            List<String> lines = Files.readAllLines(storageFile, StandardCharsets.UTF_8);
            for (String line : lines) {
                if (line.isBlank()) {
                    continue;
                }
                ScheduledJob job = deserialize(line);
                cache.put(job.getJobId(), job);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load jobs from " + storageFile, ex);
        }
    }

    private void flushToDisk() {
        try {
            if (storageFile.getParent() != null) {
                Files.createDirectories(storageFile.getParent());
            }
            List<String> lines = cache.values().stream()
                    .map(this::serialize)
                    .collect(Collectors.toCollection(ArrayList::new));
            Files.write(storageFile, lines, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to persist jobs to " + storageFile, ex);
        }
    }

    private String serialize(ScheduledJob job) {
        Trigger trigger = job.getTrigger();
        String triggerType;
        String triggerValue;
        if (trigger instanceof FixedDelayTrigger fixedDelay) {
            triggerType = "fixed_delay";
            triggerValue = String.valueOf(fixedDelay.getDelay().toMillis());
        } else if (trigger instanceof FixedRateTrigger fixedRate) {
            triggerType = "fixed_rate";
            triggerValue = String.valueOf(fixedRate.getPeriod().toMillis());
        } else if (trigger instanceof CronTrigger cronTrigger) {
            triggerType = "cron";
            triggerValue = cronTrigger.getCronExpression().getExpression().replace(" ", "_");
        } else {
            throw new IllegalStateException("Unsupported trigger type: " + trigger.getClass().getName());
        }

        RetryPolicy retryPolicy = job.getRetryPolicy();
        String retryAttempts = "0";
        String retryDelay = "0";
        if (retryPolicy instanceof MaxAttemptsRetryPolicy maxAttempts) {
            retryAttempts = String.valueOf(maxAttempts.getMaxAttempts());
            retryDelay = String.valueOf(maxAttempts.getDelay().toMillis());
        }

        String taskType = job.getTask().getMetadata().getAttribute("taskType");
        if (taskType == null) {
            taskType = job.getTask().getMetadata().getName();
        }

        return String.join("|",
                job.getJobId(),
                taskType,
                job.getTask().getId(),
                triggerType,
                triggerValue,
                retryAttempts,
                retryDelay,
                job.getNextFireTime().toString(),
                job.getState().name(),
                job.getTask().getMetadata().getName(),
                job.getTask().getMetadata().getGroup()
        );
    }

    private ScheduledJob deserialize(String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length < 11) {
            throw new IllegalStateException("Invalid job record: " + line);
        }

        String jobId = parts[0];
        String taskType = parts[1];
        String triggerType = parts[3];
        String triggerValue = parts[4];
        int retryAttempts = Integer.parseInt(parts[5]);
        long retryDelayMs = Long.parseLong(parts[6]);
        Instant nextFireTime = Instant.parse(parts[7]);
        JobState state = JobState.valueOf(parts[8]);

        Task task = taskFactory.create(taskType);

        Trigger trigger = createTrigger(triggerType, triggerValue);
        RetryPolicy retryPolicy = retryAttempts > 0
                ? new MaxAttemptsRetryPolicy(retryAttempts, Duration.ofMillis(retryDelayMs))
                : RetryPolicy.noRetry();

        ScheduledJob job = new ScheduledJob(jobId, task, trigger, retryPolicy, nextFireTime);
        job.setState(state);
        job.setTriggerContext(TriggerContext.initial(nextFireTime));
        return job;
    }

    private Trigger createTrigger(String triggerType, String triggerValue) {
        return switch (triggerType) {
            case "fixed_delay" -> new FixedDelayTrigger(Duration.ofMillis(Long.parseLong(triggerValue)));
            case "fixed_rate" -> new FixedRateTrigger(Duration.ofMillis(Long.parseLong(triggerValue)));
            case "cron" -> new CronTrigger(triggerValue.replace("_", " "));
            default -> throw new IllegalStateException("Unknown trigger type: " + triggerType);
        };
    }
}
