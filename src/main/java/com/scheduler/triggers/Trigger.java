package com.scheduler.triggers;

import com.scheduler.core.TriggerContext;

import java.time.Instant;
import java.util.Optional;

public interface Trigger {

    Optional<Instant> nextFireTime(TriggerContext context);

    boolean mayFireAgain();
}
