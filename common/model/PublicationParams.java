package com.bbva.enoa.platformservices.coreservice.common.model;

import com.bbva.enoa.datamodel.model.common.enumerates.Platform;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PublicationParams
{
    private Integer id;
    private Platform platform;
    private String releaseName;
    private String environment;
    private ReleaseVersion releaseVersion;
    /**
     * This value could be BEHAVIOR_TEST OR DEPLOYMENT
     */
    private String entityType;


    private List<ServicePublicationParams> servicePublicationParamsList;
}
