package com.cvflow.cv.model;

import java.util.Objects;
import java.util.UUID;

public class Skill {
    private UUID id;
    private String name;
    private String category;

    public Skill() {}

    public Skill(UUID id, String name, String category) {
        this.id = id;
        this.name = name;
        this.category = category;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Skill that)) {
            return false;
        }
        return Objects.equals(id, that.id)
                && Objects.equals(name, that.name)
                && Objects.equals(category, that.category);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, category);
    }
}
