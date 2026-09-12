package com.cvflow.cv.model;

import java.util.Objects;
import java.util.UUID;

public class Education {
    private UUID id;
    private String institution;
    private String degree;
    private String field;
    private String location;
    private String startDate;
    private String endDate;
    private String description;

    public Education() {}

    public Education(
            UUID id,
            String institution,
            String degree,
            String field,
            String location,
            String startDate,
            String endDate,
            String description) {
        this.id = id;
        this.institution = institution;
        this.degree = degree;
        this.field = field;
        this.location = location;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getInstitution() {
        return institution;
    }

    public void setInstitution(String institution) {
        this.institution = institution;
    }

    public String getDegree() {
        return degree;
    }

    public void setDegree(String degree) {
        this.degree = degree;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Education that)) {
            return false;
        }
        return Objects.equals(id, that.id)
                && Objects.equals(institution, that.institution)
                && Objects.equals(degree, that.degree)
                && Objects.equals(field, that.field)
                && Objects.equals(location, that.location)
                && Objects.equals(startDate, that.startDate)
                && Objects.equals(endDate, that.endDate)
                && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, institution, degree, field, location, startDate, endDate, description);
    }
}
