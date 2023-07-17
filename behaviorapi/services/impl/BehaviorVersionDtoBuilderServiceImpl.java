package com.bbva.enoa.platformservices.coreservice.behaviorapi.services.impl;

import com.bbva.enoa.apirestgen.toolsapi.model.TOSubsystemDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import com.bbva.enoa.datamodel.model.behavior.entities.BehaviorService;
import com.bbva.enoa.datamodel.model.behavior.entities.BehaviorSubsystem;
import com.bbva.enoa.datamodel.model.behavior.entities.BehaviorVersion;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.platformservices.coreservice.behaviorapi.services.interfaces.IBehaviorVersionDtoBuilder;
import com.bbva.enoa.platformservices.coreservice.behaviorapi.services.interfaces.IBehaviorVersionDtoBuilderService;
import com.bbva.enoa.platformservices.coreservice.common.model.param.ServiceOperationParams;
import com.bbva.enoa.platformservices.coreservice.common.repositories.PropertyDefinitionRepository;
import com.bbva.enoa.platformservices.coreservice.consumers.impl.ConfigurationmanagerClient;
import com.bbva.enoa.platformservices.coreservice.consumers.impl.MailServiceClient;
import com.bbva.enoa.platformservices.coreservice.consumers.impl.TodoTaskServiceClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IContinuousintegrationClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IToolsClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IVersioncontrolsystemClient;
import com.bbva.enoa.platformservices.coreservice.qualitymanagerapi.services.interfaces.IQualityManagerService;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.intefaces.IIssueTrackerService;
import com.bbva.enoa.utils.clientsutils.services.interfaces.IErrorTaskManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

/**
 * Created by xe30000 on 21/02/2017.
 */
@RefreshScope
@Service
@Slf4j
public class BehaviorVersionDtoBuilderServiceImpl implements IBehaviorVersionDtoBuilderService
{

    private static final String SUBSYSTEM_BUILDSTATUS = "subsystemBuildStatus";

    /**
     * NOVA email
     */
    @Value("${nova.alert.email:enoa.cib@bbva.com}")
    private String email;

    @Value("${nova.issuetracker.enabled:false}")
    private boolean isIssueTrackerEnabled;

    /**
     * User
     */
    @Value("${nova.alert.user:IMM0589}")
    private String user;

    /**
     * Continuous integration client
     */
    @Autowired
    private IContinuousintegrationClient iContinuousintegrationClient;

    /**
     * Quality information service
     */
    @Autowired
    private IQualityManagerService qualityManagerService;

    /**
     * Property definition repository
     */
    @Autowired
    private PropertyDefinitionRepository propertyDefinitionRepository;

    @Autowired
    private ConfigurationmanagerClient configurationmanagerClient;

    /**
     * Issue tracker service
     */
    @Autowired
    private IIssueTrackerService iIssueTrackerService;

    /**
     * Service for creating task error
     */
    @Autowired
    private IErrorTaskManager errorTaskManager;

    /**
     * To do task service client
     */
    @Autowired
    private TodoTaskServiceClient todoTaskServiceClient;

    /**
     * Tools service client
     */
    @Autowired
    private IToolsClient toolsClient;

    /**
     * version control client
     */
    @Autowired
    private IVersioncontrolsystemClient versioncontrolsystemClient;

    /**
     * Mail
     */
    @Autowired
    private MailServiceClient mailService;

    /**
     * Behavior version DTO builder
     */
    @Autowired
    private IBehaviorVersionDtoBuilder behaviorVersionDtoBuilder;

    @Override
    public void processTemplates(final BehaviorVersion behaviorVersion, final String ivUser)
    {
        log.debug("[{}] -> [{}]: process templates for behavior version [{}]",
                "BehaviorVersionDtoBuilderServiceImpl", "processTemplates", behaviorVersion.getVersionName());
        for (BehaviorSubsystem behaviorSubsystem : behaviorVersion.getSubsystems())
        {
            // Process for each service the configuration
            for (BehaviorService behaviorService : behaviorSubsystem.getServices())
            {
                // Set and save the property definition list
                this.setAndSavePropertyDefinition(behaviorSubsystem, behaviorService, ServiceType.valueOf(behaviorService.getServiceType()), ivUser);
            }
        }
    }

    @Override
    public void buildSubsystems(final Product product, final BehaviorVersion behaviorVersion, final String ivUser)
    {
        this.iContinuousintegrationClient.buildBehaviorSubsystems(product, behaviorVersion, ivUser);
    }


    //////////////////////////////////////////   PRIVATE METHODS   \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

    /**
     * Set the definition property list of the behavior version service depending on the service type
     *
     * @param behaviorSubsystem behavior version subsystem
     * @param behaviorService   behavior version service to set the properties
     * @param serviceType       the service type
     * @param ivUser            user requester
     */
    private void setAndSavePropertyDefinition(final BehaviorSubsystem behaviorSubsystem, final BehaviorService behaviorService,
                                              final ServiceType serviceType, final String ivUser)
    {
        TOSubsystemDTO subsystemDTO = this.toolsClient.getSubsystemById(behaviorSubsystem.getSubsystemId());

        String serviceName = behaviorService.getServiceName();
        log.debug("[{}] -> [{}]: setting the properties definition of the behavior version service: [{}]" +
                        " of the subsystem: [{}]. Service type: [{}] for UserCode requester: [{}]", "BehaviorVersionDtoBuilderServiceImpl",
                "setAndSavePropertyDefinition", serviceName, subsystemDTO.getSubsystemName(),
                serviceType, ivUser);

        ServiceOperationParams.ServiceOperationParamsBuilder paramsBuilder = (new ServiceOperationParams.ServiceOperationParamsBuilder())
                .tag(behaviorSubsystem.getTagName()).versionControlServiceId(subsystemDTO.getRepoId())
                .ivUser(ivUser).serviceType(serviceType.name())
                .releaseVersionServiceId(behaviorService.getId())
                .releaseVersionServiceName(serviceName);

        String modulePath = behaviorService.getFolder();
        switch (serviceType)
        {
            case BUSINESS_LOGIC_BEHAVIOR_TEST_JAVA:
                paramsBuilder.modulePath(modulePath);
                this.configurationmanagerClient.setPropertiesBehaviorService(paramsBuilder.build());
                break;

            default:
                log.debug("[{}] -> [{}]: the behavior version service: [{}] of the subsystem: [{}] is" +
                                " of Service Type: [{}] does not have properties definition - does not apply. UserCode: [{}]",
                        "BehaviorVersionDtoBuilderServiceImpl", "setAndSavePropertyDefinition", serviceName,
                        subsystemDTO.getSubsystemName(), serviceType, ivUser);
                break;
        }
    }

}
