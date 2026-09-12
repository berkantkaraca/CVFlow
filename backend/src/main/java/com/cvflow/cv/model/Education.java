package com.cvflow.cv.model;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Education {
    private UUID id;
    private String institution;
    private String degree;
    private String field;
    private String location;
    private String startDate;
    private String endDate;
    private String description;
}
