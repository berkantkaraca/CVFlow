package com.cvflow.cv.model;

import java.util.Objects;

public class PersonalInfo {
    private String fullName;
    private String title;
    private String email;
    private String phone;
    private String location;
    private String website;
    private String linkedin;
    private String github;

    public PersonalInfo() {}

    public PersonalInfo(
            String fullName,
            String title,
            String email,
            String phone,
            String location,
            String website,
            String linkedin,
            String github) {
        this.fullName = fullName;
        this.title = title;
        this.email = email;
        this.phone = phone;
        this.location = location;
        this.website = website;
        this.linkedin = linkedin;
        this.github = github;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getLinkedin() {
        return linkedin;
    }

    public void setLinkedin(String linkedin) {
        this.linkedin = linkedin;
    }

    public String getGithub() {
        return github;
    }

    public void setGithub(String github) {
        this.github = github;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PersonalInfo that)) {
            return false;
        }
        return Objects.equals(fullName, that.fullName)
                && Objects.equals(title, that.title)
                && Objects.equals(email, that.email)
                && Objects.equals(phone, that.phone)
                && Objects.equals(location, that.location)
                && Objects.equals(website, that.website)
                && Objects.equals(linkedin, that.linkedin)
                && Objects.equals(github, that.github);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fullName, title, email, phone, location, website, linkedin, github);
    }
}
