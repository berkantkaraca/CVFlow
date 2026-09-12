package com.cvflow.cv.model;

import java.util.Objects;
import java.util.UUID;

public class Link {
    private UUID id;
    private String label;
    private String url;

    public Link() {}

    public Link(UUID id, String label, String url) {
        this.id = id;
        this.label = label;
        this.url = url;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
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
        if (!(o instanceof Link that)) {
            return false;
        }
        return Objects.equals(id, that.id)
                && Objects.equals(label, that.label)
                && Objects.equals(url, that.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, label, url);
    }
}
