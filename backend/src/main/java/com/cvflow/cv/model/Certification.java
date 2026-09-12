package com.cvflow.cv.model;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Certification {
    private UUID id;
    private String name;
    private String issuer;
    private String date;
    private String url;
}
