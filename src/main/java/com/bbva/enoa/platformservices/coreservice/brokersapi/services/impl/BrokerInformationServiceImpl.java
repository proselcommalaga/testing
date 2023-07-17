package com.bbva.enoa.platformservices.coreservice.brokersapi.services.impl;

import com.bbva.enoa.apirestgen.brokeragentapi.model.ConnectionDTO;
import com.bbva.enoa.apirestgen.brokersapi.model.*;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.datamodel.model.broker.entities.Broker;
import com.bbva.enoa.datamodel.model.broker.entities.BrokerUser;
import com.bbva.enoa.datamodel.model.broker.enumerates.BrokerStatus;
import com.bbva.enoa.datamodel.model.broker.enumerates.BrokerType;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.platformservices.coreservice.brokersapi.services.interfaces.IBrokerBuilder;
import com.bbva.enoa.platformservices.coreservice.brokersapi.services.interfaces.IBrokerInformationService;
import com.bbva.enoa.platformservices.coreservice.brokersapi.services.interfaces.IBrokerValidator;
import com.bbva.enoa.platformservices.coreservice.common.repositories.BrokerRepository;
import com.bbva.enoa.platformservices.coreservice.consumers.novaagent.interfaces.IBrokerAgentApiClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * class to access broker information
 */
@Service
public class BrokerInformationServiceImpl implements IBrokerInformationService
{
    /**
     * Explanatory message for valid number of nodes by environment
     */
    public static final String COMMENT_FOR_INT_MONOCPD = "Puedes elegir 1 o 2 nodos según el grado de disponibilidad que prefieras.";
    public static final String COMMENT_FOR_INT_MULTICPD = "Puedes elegir 1, 2 o 3 nodos según el grado de disponibilidad que prefieras.";
    public static final String COMMENT_FOR_PRE_MONOCPD = "El producto está configurado con infraestructura de despliegue en producción en modo Mono CPD. Puedes elegir 1 o 2 nodos según el grado de " +
            "disponibilidad que prefieras.";
    public static final String COMMENT_FOR_PRE_MULTICPD = "El producto está configurado con infraestructura de despliegue en producción en modo Multi CPD. Debes elegir al menos 2 nodos para que sea" +
            " compatible con el número de nodos válido para producción.";
    public static final String COMMENT_FOR_PRO_MONOCPD_TC = "El producto está configurado con infraestructura de despliegue en modo Mono CPD TC. Puedes elegir 1 o 2 nodos según el grado de " +
            "disponibilidad que prefieras. Todos los nodos se desplegarán en el CPD de Tres Cantos.";
    public static final String COMMENT_FOR_PRO_MONOCPD_V = "El producto está configurado con infraestructura de despliegue en modo Mono CPD V. Puedes elegir 1 o 2 nodos según el grado de " +
            "disponibilidad que prefieras. Todos los nodos se desplegarán en el CPD de Vaguada.";
    public static final String COMMENT_FOR_PRO_MULTICPD = "El producto está configurado con infraestructura de despliegue en modo Multi CPD. Debes elegir al menos 2 nodos para que se despliegue al " +
            "menos uno en cada CPD.";
    public static final String DEFAULT_EXCHANGES_PREFIX = "amq.";

    /**
     * JPA repository for Broker
     */
    private final BrokerRepository brokerRepository;

    /**
     * Broker builder
     */
    private final IBrokerBuilder brokerBuilder;

    /**
     * Broker validator
     */
    private final IBrokerValidator brokerValidator;
    /**
     * Broker Agent client
     */
    private final IBrokerAgentApiClient brokerAgentClient;

    public BrokerInformationServiceImpl(final BrokerRepository brokerRepository, final IBrokerBuilder brokerBuilder, final IBrokerValidator brokerValidator, final IBrokerAgentApiClient brokerAgentClient)
    {
        this.brokerRepository = brokerRepository;
        this.brokerBuilder = brokerBuilder;
        this.brokerValidator = brokerValidator;
        this.brokerAgentClient = brokerAgentClient;
    }

    @Override
    @Transactional(readOnly = true)
    public BrokerDTO[] getBrokersByProduct(Integer productId)
    {
        List<Broker> brokers = this.brokerRepository.findByProductId(productId);
        return brokers.stream().map(this.brokerBuilder::buildBasicBrokerDTOFromEntity)
                .toArray(BrokerDTO[]::new);
    }

    @Override
    @Transactional(readOnly = true)
    public BrokerDTO getBrokerInfo(String ivUser, Integer brokerId)
    {
        Broker broker = this.brokerValidator.validateAndGetBroker(brokerId);
        return this.brokerBuilder.buildBrokerDTOFromEntity(ivUser, broker);
    }

    @Override
    @Transactional(readOnly = true)
    public ValidNumberOfNodesByEnvironmentDTO getValidNumberOfNodesInfo(Integer productId)
    {
        Product product = this.brokerValidator.validateAndGetProduct(productId);

        boolean isMonoCPD = !product.getMultiCPDInPro();
        String cpdName = isMonoCPD ? product.getCPDInPro().getName() : null;

        ValidNumberOfNodesByEnvironmentDTO info = new ValidNumberOfNodesByEnvironmentDTO();
        info.setValidNumberOfNodesForInt(getValidNumberOfNodesInfo(Environment.INT, isMonoCPD, cpdName));
        info.setValidNumberOfNodesForPre(getValidNumberOfNodesInfo(Environment.PRE, isMonoCPD, cpdName));
        info.setValidNumberOfNodesForPro(getValidNumberOfNodesInfo(Environment.PRO, isMonoCPD, cpdName));

        return info;
    }

    @Override
    public QueueInfoDTO[] getQueuesInfo(final Integer brokerId, final Integer age)
    {
        // validations
        Broker broker = this.brokerValidator.validateAndGetBroker(brokerId);
        BrokerUser brokerUser = this.brokerValidator.validateAndGetBrokerAdminUser(broker);
        ConnectionDTO connectionDTO = this.brokerBuilder.buildConnectionDTOFromBrokerUserAndBrokerList(brokerUser, broker.getNodes());

        // execute operation
        return this.brokerAgentClient.getQueuesInfo(broker.getEnvironment(), connectionDTO, age);
    }


    @Override
    public ExchangeInfoDTO[] getExchangesInfo(final Integer brokerId, final Integer age)
    {
        // validations
        Broker broker = this.brokerValidator.validateAndGetBroker(brokerId);
        this.brokerValidator.validateBrokerCanBeOperable(broker);
        BrokerUser brokerUser = this.brokerValidator.validateAndGetBrokerAdminUser(broker);
        ConnectionDTO connectionDTO = this.brokerBuilder.buildConnectionDTOFromBrokerUserAndBrokerList(brokerUser, broker.getNodes());
        ExchangeInfoDTO[] exchangeInfoDTOS = this.brokerAgentClient.getExchangesInfo(broker.getEnvironment(), connectionDTO, age);

        // execute operation
        return this.getExchangeInfoDTOSFiltereds(exchangeInfoDTOS);
    }

    @Override
    public String[] getBrokerStatuses()
    {
        return Arrays.stream(BrokerStatus.values()).map(Enum::toString).toArray(String[]::new);
    }

    @Override
    public String[] getBrokerTypes()
    {
        return Arrays.stream(BrokerType.values()).map(Enum::toString).toArray(String[]::new);
    }

    /**
     * Filter exchanges valids to frontend.
     *
     * <p>Wich name is not null</p>
     * <p>Wich name is not empty</p>
     * <p>Wich is not a default eschange</p>
     *
     * @param exchangeInfoDTOS list of exchanges to filter
     * @return array of exchangeInfoDTO filtereds
     */
    private ExchangeInfoDTO[] getExchangeInfoDTOSFiltereds(final ExchangeInfoDTO[] exchangeInfoDTOS)
    {
        Stream<ExchangeInfoDTO> exchangeInfoDTOFilteredList = Arrays.stream(exchangeInfoDTOS)
                .filter(exchangeInfoDTO ->
                        exchangeInfoDTO.getName() != null
                                && !exchangeInfoDTO.getName().isEmpty()
                                && !exchangeInfoDTO.getName().contains(DEFAULT_EXCHANGES_PREFIX));
        return exchangeInfoDTOFilteredList.toArray(ExchangeInfoDTO[]::new);
    }

    private ValidNumberOfNodesInfoDTO getValidNumberOfNodesInfo(Environment environment, boolean isMonoCPD, String cpdName)
    {
        ValidNumberOfNodesInfoDTO validNodesInfo = new ValidNumberOfNodesInfoDTO();
        validNodesInfo.setValidNumberOfNodes(this.brokerValidator.getValidNumberOfNodes(environment, isMonoCPD));
        validNodesInfo.setComment(getCommentForValidNumberOfNodes(environment, isMonoCPD, cpdName));
        return validNodesInfo;
    }

    private String getCommentForValidNumberOfNodes(Environment environment, boolean isMonoCPD, String cpdName)
    {
        switch (environment)
        {
            case INT:
                return isMonoCPD ? COMMENT_FOR_INT_MONOCPD : COMMENT_FOR_INT_MULTICPD;
            case PRE:
                return isMonoCPD ? COMMENT_FOR_PRE_MONOCPD : COMMENT_FOR_PRE_MULTICPD;
            case PRO:
                if (!isMonoCPD)
                {
                    return COMMENT_FOR_PRO_MULTICPD;
                }
                else
                {
                    return cpdName.equals("TC") ? COMMENT_FOR_PRO_MONOCPD_TC : COMMENT_FOR_PRO_MONOCPD_V;
                }
            default:
                return null;
        }
    }

}
