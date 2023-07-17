package com.bbva.enoa.platformservices.coreservice.brokersapi.services.interfaces;

import com.bbva.enoa.datamodel.model.broker.entities.Broker;
import com.bbva.enoa.datamodel.model.broker.entities.BrokerProperty;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;

import java.util.List;
import java.util.Map;


/**
 * The interface Broker properties configuration.
 */
public interface IBrokerPropertiesConfiguration
{

    /**
     * Gets broker SCS property list that depends only on associated broker.
     *
     * @param broker the broker
     * @return broker property list
     */
    List<BrokerProperty> getBrokerSCSPropertiesDependentOnBroker(Broker broker);

    /**
     * Gets broker SCS property list that depends only on release version service.
     *
     * @param releaseVersionService the release version service
     * @return the broker scs properties dependent on release version service
     */
    Map<String, String> getBrokerSCSPropertiesDependentOnReleaseVersionService(ReleaseVersionService releaseVersionService);

    /**
     * Gets broker SCS property list that depends on service and broker.
     *
     * @param releaseVersionService the release version service
     * @param brokerList            the broker list
     * @return the broker scs properties dependent on service and broker
     */
    List<BrokerProperty> getBrokerSCSPropertiesDependentOnServiceAndBroker(ReleaseVersionService releaseVersionService, Broker brokerList);
}
