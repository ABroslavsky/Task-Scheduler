package com.scheduler.persistence;

import com.scheduler.core.ScheduledJob;

import java.util.Collection;
import java.util.Optional;

public interface JobRepository {

    void save(ScheduledJob job);

    Optional<ScheduledJob> findById(String jobId);

    Collection<ScheduledJob> findAll();

    void delete(String jobId);
}
