# Task Scheduler

Java scheduler with fixed delay, fixed rate, and cron triggers.

Requirements: Java 17, Gradle.

## Quick start

```bash
./gradlew run
```

The demo schedules four jobs for about 12 seconds: fixed delay, fixed rate (paused/resumed mid-run), retry, and cron. Heartbeats append lines to `data/heartbeat.log`; job definitions go to `data/jobs.dat`.

## Basic usage

```java
Scheduler scheduler = SchedulerFactory.create();

scheduler.addListener(event -> {
    // TaskScheduled, TaskStarted, TaskCompleted, TaskFailed
});

scheduler.schedule(JobBuilder.create()
    .task(new MyTask())
    .withTrigger(TriggerFactory.fixedDelay(Duration.ofSeconds(5)))
    .withRetry(RetryPolicy.maxAttempts(3, Duration.ofSeconds(1)))
    .build());

scheduler.start();
// ...
scheduler.shutdown();
```

## Cron

Five fields: `minute hour day month dayOfWeek`

| Syntax | Meaning |
|--------|---------|
| `*` | any value |
| `*/N` | every N |
| `a-b` | range |
| `a,b` | list |

Day of week: `0`–`7` or `SUN`–`SAT` (`0` and `7` are Sunday).

Example: `0 */1 * * *` — at second 0 of every minute.

## Layout

```
com.scheduler/
  core/         engine, job builder, scheduler facade
  tasks/        task interface and command wrapper
  triggers/     fixed delay, fixed rate, cron
  execution/    thread pool
  events/       lifecycle events and event bus
  retry/        retry policies
  factory/      scheduler and job factories
  persistence/  in-memory and file storage
  decorator/    logging, metrics, retry wrappers
  demo/         runnable example
```

Planning and execution are split: one orchestrator thread picks due jobs, a thread pool runs them.

## File storage

Swap the default in-memory repository for file-based storage:

```java
TaskFactory taskFactory = new TaskFactory();
taskFactory.register("my-task", MyTask::new);

JobRepository repository = new FileJobRepository(
    Path.of("data/jobs.dat"),
    taskFactory
);

Scheduler scheduler = SchedulerFactory.create(SchedulerConfig.defaults(), repository);
```

Tasks serialized to disk need a `taskType` entry in `TaskMetadata` attributes.
