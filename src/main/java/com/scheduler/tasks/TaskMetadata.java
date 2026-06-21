package com.scheduler.tasks;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class TaskMetadata {

    private final String name;
    private final String group;
    private final String description;
    private final Map<String, String> attributes;

    private TaskMetadata(String name, String group, String description, Map<String, String> attributes) {
        this.name = name;
        this.group = group;
        this.description = description;
        this.attributes = Collections.unmodifiableMap(new HashMap<>(attributes));
    }

    public static TaskMetadata of(String name, String group, String description) {
        return new TaskMetadata(name, group, description, Map.of());
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getName() {
        return name;
    }

    public String getGroup() {
        return group;
    }

    public String getDescription() {
        return description;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public String getAttribute(String key) {
        return attributes.get(key);
    }

    public static final class Builder {

        private String name = "";
        private String group = "default";
        private String description = "";
        private final Map<String, String> attributes = new HashMap<>();

        public Builder name(String name) {
            this.name = Objects.requireNonNull(name);
            return this;
        }

        public Builder group(String group) {
            this.group = Objects.requireNonNull(group);
            return this;
        }

        public Builder description(String description) {
            this.description = Objects.requireNonNull(description);
            return this;
        }

        public Builder attribute(String key, String value) {
            attributes.put(key, value);
            return this;
        }

        public TaskMetadata build() {
            return new TaskMetadata(name, group, description, attributes);
        }
    }
}
