package com.cvflow.cv.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Project {
    private UUID id;
    private String name;
    private String description;
    private List<String> technologies = new ArrayList<>();
    private String url;

    public Project() {}

    public Project(UUID id, String name, String description, List<String> technologies, String url) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.technologies = technologies == null ? new ArrayList<>() : technologies;
        this.url = url;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getTechnologies() {
        return technologies;
    }

    public void setTechnologies(List<String> technologies) {
        this.technologies = technologies == null ? new ArrayList<>() : technologies;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Project that)) {
            return false;
        }
        return Objects.equals(id, that.id)
                && Objects.equals(name, that.name)
                && Objects.equals(description, that.description)
                && Objects.equals(technologies, that.technologies)
                && Objects.equals(url, that.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, technologies, url);
    }
}
