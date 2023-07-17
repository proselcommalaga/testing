package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class NovaYmlRequirement {
    /**
     * Requirement name
     */
    private String name;

    /**
     * Requirement value
     */
    private String value;

    /**
     * Requirement type
     */
    private String type;

    /**
     * Requirement description
     */
    private String description;
}
