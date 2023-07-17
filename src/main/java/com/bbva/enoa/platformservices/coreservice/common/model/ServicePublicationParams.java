package com.bbva.enoa.platformservices.coreservice.common.model;

import com.bbva.enoa.datamodel.model.api.entities.ApiImplementation;
import com.bbva.enoa.datamodel.model.api.entities.ApiVersion;
import com.bbva.enoa.datamodel.model.common.enumerates.Platform;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ServicePublicationParams
{
    private Integer versionServiceId;
    private String groupId;
    private String artifactId;
    private String version;
    private String serviceType;
    private Platform selectDeploy;
    private String releaseName;
    private String productUuaa;
    private String environment;
    private String dockerKey;
    private List<ApiImplementation<?, ?, ?>> servedApisList;
    private List<ApiVersion<?, ?, ?>> consumedApiList;
}
