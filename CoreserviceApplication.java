package com.bbva.enoa.platformservices.coreservice;

import com.bbva.enoa.core.novaclientha.app.AbstractHAAsyncNovaApp;
import com.bbva.enoa.datamodel.model.EntitiesBasePackage;
import com.bbva.enoa.platformservices.coreservice.common.repositories.RepositoriesBasePackage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * This is the main entry point to your application.
 *
 * @author BBVA - BBVA
 */
@EnableFeignClients(basePackages = {
        "com.bbva.enoa.apirestgen.continuousintegrationapi.client.feign.nova.rest",
        "com.bbva.enoa.apirestgen.versioncontrolsystemapi.client.feign.nova.rest",
        "com.bbva.enoa.apirestgen.mailserviceapi.client.feign.nova.rest",
        "com.bbva.enoa.apirestgen.deploymentmanagerapi.client.feign.nova.rest",
        "com.bbva.enoa.apirestgen.qualityassuranceapi.client.feign.nova.rest",
        "com.bbva.enoa.apirestgen.filesystemmanagerapi.client.feign.nova.rest",
        "com.bbva.enoa.apirestgen.configurationmanagerapi.client.feign.nova.rest",
        "com.bbva.enoa.apirestgen.dockerregistryapi.client.feign.nova.rest",
        "com.bbva.enoa.apirestgen.productbudgetsapi.client.feign.nova.rest",
        "com.bbva.enoa.apirestgen.issuetrackerapi.client.feign.nova.rest",
        "com.bbva.enoa.apirestgen.documentsmanagerapi.client.feign.nova.rest",
        "com.bbva.enoa.apirestgen.apigatewaymanagerapi.client.feign.nova.rest",
        "com.bbva.enoa.apirestgen.toolsapi.client.feign.nova.rest",
        "com.bbva.enoa.apirestgen.librarymanagerapi.client.feign.nova.rest",
        "com.bbva.enoa.apirestgen.schedulermanagerapi.client.feign.nova.rest",
        "com.bbva.enoa.apirestgen.ethermanagerapi.client.feign.nova.rest",
        "com.bbva.enoa.apirestgen.statisticsuserapi.client.feign.nova.rest",
        "com.bbva.enoa.apirestgen.usersadminapi.client.feign.nova.rest",
        "com.bbva.enoa.apirestgen.schedulecontrolmapi.client.feign.nova.rest",
        "com.bbva.enoa.apirestgen.batchmanagerapi.client.feign.nova.rest",
        "com.bbva.enoa.apirestgen.alertserviceapi.client.feign.nova.rest",
        "com.bbva.enoa.apirestgen.filetransferstatisticsapi.client.feign.nova.rest",
        "com.bbva.enoa.apirestgen.logsapi.client.feign.nova.rest",
        "com.bbva.enoa.apirestgen.filetransferadmin.client.feign.nova.rest",
        "com.bbva.enoa.apirestgen.brokerdeploymentapi.client.feign.nova.rest",
        "com.bbva.enoa.apirestgen.filesystemsapi.client.feign.nova.rest",
        "com.bbva.enoa.apirestgen.behaviormanagerapi.client.feign.nova.rest"})
@EnableJpaRepositories(basePackageClasses = RepositoriesBasePackage.class)
@EntityScan(basePackageClasses = EntitiesBasePackage.class)
public class CoreserviceApplication extends AbstractHAAsyncNovaApp
{
    /**
     * Main class of the core service. Spring boot run
     *
     * @param args CoreserviceApplication class
     */
    public static void main(String[] args)
    {
        SpringApplication.run(CoreserviceApplication.class, args);
    }
}
