package com.scheduler.core;

public final class SchedulerConfig {

    private final int corePoolSize;
    private final int maxPoolSize;
    private final int queueCapacity;
    private final long keepAliveSeconds;
    private final long orchestratorTickMs;

    private SchedulerConfig(Builder builder) {
        this.corePoolSize = builder.corePoolSize;
        this.maxPoolSize = builder.maxPoolSize;
        this.queueCapacity = builder.queueCapacity;
        this.keepAliveSeconds = builder.keepAliveSeconds;
        this.orchestratorTickMs = builder.orchestratorTickMs;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static SchedulerConfig defaults() {
        return builder().build();
    }

    public int getCorePoolSize() {
        return corePoolSize;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public int getQueueCapacity() {
        return queueCapacity;
    }

    public long getKeepAliveSeconds() {
        return keepAliveSeconds;
    }

    public long getOrchestratorTickMs() {
        return orchestratorTickMs;
    }

    public static final class Builder {

        private int corePoolSize = 4;
        private int maxPoolSize = 8;
        private int queueCapacity = 256;
        private long keepAliveSeconds = 60;
        private long orchestratorTickMs = 50;

        public Builder corePoolSize(int corePoolSize) {
            this.corePoolSize = corePoolSize;
            return this;
        }

        public Builder maxPoolSize(int maxPoolSize) {
            this.maxPoolSize = maxPoolSize;
            return this;
        }

        public Builder queueCapacity(int queueCapacity) {
            this.queueCapacity = queueCapacity;
            return this;
        }

        public Builder keepAliveSeconds(long keepAliveSeconds) {
            this.keepAliveSeconds = keepAliveSeconds;
            return this;
        }

        public Builder orchestratorTickMs(long orchestratorTickMs) {
            this.orchestratorTickMs = orchestratorTickMs;
            return this;
        }

        public SchedulerConfig build() {
            if (corePoolSize < 1 || maxPoolSize < corePoolSize || queueCapacity < 1) {
                throw new IllegalStateException("Invalid thread pool configuration");
            }
            return new SchedulerConfig(this);
        }
    }
}
