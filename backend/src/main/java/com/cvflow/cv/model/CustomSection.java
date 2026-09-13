package com.cvflow.cv.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User-defined CV section (e.g. Hobbies, Volunteering, Publications). Rendered after the
 * built-in ordered sections, in list order. Not addressable by AI operations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomSection {
    private UUID id;
    private String title;
    private List<String> items = new ArrayList<>();
}
