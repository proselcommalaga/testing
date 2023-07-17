package com.bbva.enoa.platformservices.coreservice.consumers.impl;

import com.bbva.enoa.apirestgen.deploymentmanagerapi.client.feign.nova.rest.IRestHandlerDeploymentmanagerapi;
import com.bbva.enoa.apirestgen.deploymentmanagerapi.client.feign.nova.rest.IRestListenerDeploymentmanagerapi;
import com.bbva.enoa.apirestgen.deploymentmanagerapi.client.feign.nova.rest.impl.RestHandlerDeploymentmanagerapi;
import com.bbva.enoa.apirestgen.deploymentmanagerapi.model.HostMemoryInfo;
import com.bbva.enoa.core.novabootstarter.consumers.SingleApiClientResponseWrapper;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.exceptions.DeploymentError;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;


/**
 * Report Deployment Manager Client
 */
@Slf4j
@Service
public class ReportDeploymentManagerClient
{
    @Autowired
    private IRestHandlerDeploymentmanagerapi restInterface;

    /**
     * API services.
     */
    private RestHandlerDeploymentmanagerapi restHandler;

    /**
     * Init the handler and listener.
     */
    @PostConstruct
    public void init()
    {
        this.restHandler = new RestHandlerDeploymentmanagerapi(this.restInterface);
    }

    /**
     * Report Host
     *
     * @param cluster cluster: TC_PRO, TC_PRE, TC_INT
     * @return String report resources by host
     */
    public String reportHost(String cluster)
    {
        log.debug("[ReportDeploymentManagerClient] -> [reportHost]: calling Deployment Manager Service to reportHost of cluster: [{}]", cluster);

        final SingleApiClientResponseWrapper<String> response = new SingleApiClientResponseWrapper<>();
        this.restHandler.getHostReportState(

                new IRestListenerDeploymentmanagerapi()
                {
                    @Override
                    public void getHostReportState(String cluster)
                    {
                        log.debug("[ReportDeploymentManagerClient] -> [reportHost]: launched reportHost of cluster: [{}]", cluster);
                        response.set(cluster);
                    }

                    @Override
                    public void getHostReportStateErrors(Errors outcome)
                    {
                        throw new NovaException(DeploymentError.getReportFromDeploymentManagerError("N/A", cluster), outcome.getBodyExceptionMessage().toString());
                    }
                },
                cluster);
        return response.get();
    }

    public HostMemoryInfo[] getProductsHostsReport(final String cluster, final String cpd)
    {
        log.debug("[ReportDeploymentManagerClient] -> [getProductsHostsReport]: calling Deployment Manager Service to product report Host of cluster: [{}] and cpd: [{}]", cluster, cpd);

        final SingleApiClientResponseWrapper<HostMemoryInfo[]> response = new SingleApiClientResponseWrapper<>();
        this.restHandler.getHostsMemoryInfo(

                new IRestListenerDeploymentmanagerapi()
                {
                    @Override
                    public void getHostsMemoryInfo(HostMemoryInfo[] hostMemoryInfos)
                    {
                        log.debug("[ReportDeploymentManagerClient] -> [getProductsHostsReport]: launched product report Host of cluster: [{}] and cpd: [{}]", cluster, cpd);
                        response.set(hostMemoryInfos);
                    }

                    @Override
                    public void getHostsMemoryInfoErrors(Errors outcome)
                    {
                        throw new NovaException(DeploymentError.getReportFromDeploymentManagerError(cpd, cluster), outcome.getBodyExceptionMessage().toString());
                    }
                },
                cluster, cpd);
        return response.get();
    }
}
