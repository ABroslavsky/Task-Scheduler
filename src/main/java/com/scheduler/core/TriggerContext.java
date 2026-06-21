package com.scheduler.core;

import java.time.Instant;
import java.util.Optional;

public final class TriggerContext {

    private final Instant scheduledFireTime;
    private final Instant actualFireTime;
    private final Instant previousFireTime;
    private final Instant completionTime;
    private final int refireCount;

    private TriggerContext(Instant scheduledFireTime, Instant actualFireTime,
                           Instant previousFireTime, Instant completionTime, int refireCount) {
        this.scheduledFireTime = scheduledFireTime;
        this.actualFireTime = actualFireTime;
        this.previousFireTime = previousFireTime;
        this.completionTime = completionTime;
        this.refireCount = refireCount;
    }

    public static TriggerContext initial(Instant scheduledFireTime) {
        return new TriggerContext(scheduledFireTime, null, null, null, 0);
    }

    public TriggerContext withFire(Instant actualFireTime) {
        return new TriggerContext(scheduledFireTime, actualFireTime, previousFireTime, completionTime, refireCount);
    }

    public TriggerContext withCompletion(Instant completionTime) {
        return new TriggerContext(scheduledFireTime, actualFireTime, previousFireTime, completionTime, refireCount);
    }

    public TriggerContext nextCycle(Instant nextScheduledFireTime) {
        return new TriggerContext(nextScheduledFireTime, null, actualFireTime, null, 0);
    }

    public TriggerContext withRefire(int refireCount) {
        return new TriggerContext(scheduledFireTime, actualFireTime, previousFireTime, completionTime, refireCount);
    }

    public Instant getScheduledFireTime() {
        return scheduledFireTime;
    }

    public Optional<Instant> getActualFireTime() {
        return Optional.ofNullable(actualFireTime);
    }

    public Optional<Instant> getPreviousFireTime() {
        return Optional.ofNullable(previousFireTime);
    }

    public Optional<Instant> getCompletionTime() {
        return Optional.ofNullable(completionTime);
    }

    public int getRefireCount() {
        return refireCount;
    }
}
