package com.cvflow.cv.model;

import java.util.Objects;
import java.util.UUID;

public class Certification {
    private UUID id;
    private String name;
    private String issuer;
    private String date;
    private String url;

    public Certification() {}

    public Certification(UUID id, String name, String issuer, String date, String url) {
        this.id = id;
        this.name = name;
        this.issuer = issuer;
        this.date = date;
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

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
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
        if (!(o instanceof Certification that)) {
            return false;
        }
        return Objects.equals(id, that.id)
                && Objects.equals(name, that.name)
                && Objects.equals(issuer, that.issuer)
                && Objects.equals(date, that.date)
                && Objects.equals(url, that.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, issuer, date, url);
    }
}
