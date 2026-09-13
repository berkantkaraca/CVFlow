package com.cvflow.cv.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersonalInfo {
    private String fullName;
    private String title;
    private String email;
    private String phone;
    private String location;
    private String website;
    private String linkedin;
    private String github;
    private String photoUrl;
}
