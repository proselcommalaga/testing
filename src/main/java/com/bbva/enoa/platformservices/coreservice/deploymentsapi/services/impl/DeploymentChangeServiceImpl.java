package com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.impl;

import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentChange;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentChangeRepository;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.IDeploymentChangeService;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils.DeploymentConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * DeploymentChangeService implementation
 */
@Service
public class DeploymentChangeServiceImpl implements IDeploymentChangeService
{
    /**
     * repository of deploymentChange
     */
    private final DeploymentChangeRepository deploymentChangeRepository;

    /**
     * Default constructor by params
     *
     * @param deploymentChangeRepository repository of deploymentChangeRepository
     */
    @Autowired
    public DeploymentChangeServiceImpl(final DeploymentChangeRepository deploymentChangeRepository)
    {
        this.deploymentChangeRepository = deploymentChangeRepository;
    }

    @Override
    public Page<DeploymentChange> getHistory(final Integer deploymentId, final Long pageNumber, final Long pageSize)
    {
        Pageable pagination = this.buildPagination(pageNumber, pageSize);

        return deploymentChangeRepository.getHistory(deploymentId, pagination);
    }

    /**
     * Build pagination
     *
     * @param pageNumber page number
     * @param pageSize   page size
     * @return pageable
     */
    private Pageable buildPagination(Long pageNumber, Long pageSize)
    {

        Integer currentPage = DeploymentConstants.DEFAULT_PAGE_NUMBER;
        Integer chunkSize = DeploymentConstants.DEFAULT_PAGE_SIZE;

        if (pageNumber != null)
        {
            currentPage = pageNumber.intValue();
        }
        if (pageSize != null && pageSize != 0)
        {
            chunkSize = pageSize.intValue();
        }
        return PageRequest.of(currentPage, chunkSize);
    }
}
