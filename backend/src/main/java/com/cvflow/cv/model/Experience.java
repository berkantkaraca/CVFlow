package com.cvflow.cv.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Experience {
    private UUID id;
    private String company;
    private String position;
    private String location;
    private String startDate;
    private String endDate;
    private boolean current;
    private List<String> description = new ArrayList<>();

    public Experience() {}

    public Experience(
            UUID id,
            String company,
            String position,
            String location,
            String startDate,
            String endDate,
            boolean current,
            List<String> description) {
        this.id = id;
        this.company = company;
        this.position = position;
        this.location = location;
        this.startDate = startDate;
        this.endDate = endDate;
        this.current = current;
        this.description = description == null ? new ArrayList<>() : description;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
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

    public boolean isCurrent() {
        return current;
    }

    public void setCurrent(boolean current) {
        this.current = current;
    }

    public List<String> getDescription() {
        return description;
    }

    public void setDescription(List<String> description) {
        this.description = description == null ? new ArrayList<>() : description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Experience that)) {
            return false;
        }
        return current == that.current
                && Objects.equals(id, that.id)
                && Objects.equals(company, that.company)
                && Objects.equals(position, that.position)
                && Objects.equals(location, that.location)
                && Objects.equals(startDate, that.startDate)
                && Objects.equals(endDate, that.endDate)
                && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, company, position, location, startDate, endDate, current, description);
    }
}
