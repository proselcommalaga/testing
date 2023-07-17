package com.bbva.enoa.platformservices.coreservice.common.repositories;

import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.datamodel.model.broker.entities.Broker;
import com.bbva.enoa.datamodel.model.broker.enumerates.BrokerStatus;
import com.bbva.enoa.datamodel.model.common.enumerates.Platform;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Broker repository
 */
public interface BrokerRepository extends JpaRepository<Broker, Integer>
{
    /**
     * Find all existing brokers by Product ID
     *
     * @param productId Product ID
     * @return found brokers by product, as a List of Broker objects
     */
    List<Broker> findByProductId(Integer productId);

    /**
     * Find all existing brokers by Product ID and environment
     *
     * @param productId   Product ID
     * @param environment Environment
     * @return found brokers by product, as a List of Broker objects
     */
    List<Broker> findByProductIdAndEnvironment(Integer productId, String environment);

    /**
     * Find existing broker by name and environment
     *
     * @param productId
     * @param name        the broker name
     * @param environment Environment
     * @return found brokers by product, as a List of Broker objects
     */
    Optional<Broker> findByProductIdAndNameAndEnvironment(Integer productId, String name, String environment);

    /**
     * Find all existing brokers attached to the given filesystem
     *
     * @param filesystemId Filesystem ID
     * @return found brokers by filesystem, as a List of Broker objects
     */
    List<Broker> findByFilesystemId(Integer filesystemId);

    /**
     * Find all by status
     *
     * @param status broker status
     * @return list of brokers with the given status
     */
    List<Broker> findByStatus(BrokerStatus status);

    /**
     * Find Broker by UUAA and Broker Status.
     *
     * @param productId    The given Product ID.
     * @param brokerStatus The given Broker Status.
     * @return A List of Brokers.
     */
    List<Broker> findByProductIdAndStatus(int productId, BrokerStatus brokerStatus);

    /**
     * Find Broker by Environment.
     *
     * @param environment The given Environment.
     * @return A List of Brokers.
     */
    List<Broker> findByEnvironment(String environment);

    /**
     * Gets all brokers of a product of the provided environment.
     *
     * @param productId   the product id
     * @param environment {@link Environment}
     * @return List of brokers.productId
     */
    List<Broker> findByProductIdAndEnvironment(int productId, String environment);

    List<Broker> findByIdIn(@Param("ids") Set<Integer> ids);

    List<Broker> findByProductIdAndEnvironmentAndStatus(int productId, String environment, BrokerStatus brokerStatus);

    @Query(value = "select b from Broker b " +
            "where (:environment is null or b.environment = :environment) " +
            "and (:productId is null or b.product.id = :productId) " +
            "and (:platform is null or b.platform = :platform) " +
            "and (:status is null or b.status = :status) ")
    List<Broker> findAllBrokersSummary(
            @Param("environment") String environment,
            @Param("productId") Integer productId,
            @Param("platform") Platform platform,
            @Param("status") BrokerStatus status);

}
