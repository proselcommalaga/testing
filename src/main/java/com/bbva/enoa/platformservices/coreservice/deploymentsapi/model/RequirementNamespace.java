package com.bbva.enoa.platformservices.coreservice.deploymentsapi.model;

import com.bbva.enoa.apirestgen.deploymentsapi.model.LMLibraryRequirementFulfilledDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import lombok.*;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class RequirementNamespace {

    private Integer releaseVersionServiceId;

    private Product product;

    private Environment environment;

    private DeploymentService deploymentService;

    private LMLibraryRequirementFulfilledDTO lmLibraryRequirementFulfilledDTO;
}
