package com.scheduler.factory;

import com.scheduler.core.Scheduler;
import com.scheduler.core.SchedulerConfig;
import com.scheduler.persistence.InMemoryJobRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotSame;

class SchedulerFactoryTest {

    @Test
    void eachCreateBuildsNewEngine() {
        SchedulerConfig fast = SchedulerConfig.builder().orchestratorTickMs(10).build();
        SchedulerConfig slow = SchedulerConfig.builder().orchestratorTickMs(100).build();

        Scheduler first = SchedulerFactory.create(fast, new InMemoryJobRepository());
        Scheduler second = SchedulerFactory.create(slow, new InMemoryJobRepository());

        assertNotSame(first, second);
    }
}
