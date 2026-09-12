package com.cvflow.cv.model;

import java.util.Objects;
import java.util.UUID;

public class Language {
    private UUID id;
    private String name;
    private String level;

    public Language() {}

    public Language(UUID id, String name, String level) {
        this.id = id;
        this.name = name;
        this.level = level;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Language that)) {
            return false;
        }
        return Objects.equals(id, that.id)
                && Objects.equals(name, that.name)
                && Objects.equals(level, that.level);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, level);
    }
}
