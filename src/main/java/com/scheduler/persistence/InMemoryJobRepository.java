package com.scheduler.persistence;

import com.scheduler.core.ScheduledJob;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryJobRepository implements JobRepository {

    private final ConcurrentHashMap<String, ScheduledJob> storage = new ConcurrentHashMap<>();

    @Override
    public void save(ScheduledJob job) {
        storage.put(job.getJobId(), job);
    }

    @Override
    public Optional<ScheduledJob> findById(String jobId) {
        return Optional.ofNullable(storage.get(jobId));
    }

    @Override
    public Collection<ScheduledJob> findAll() {
        return storage.values();
    }

    @Override
    public void delete(String jobId) {
        storage.remove(jobId);
    }
}
