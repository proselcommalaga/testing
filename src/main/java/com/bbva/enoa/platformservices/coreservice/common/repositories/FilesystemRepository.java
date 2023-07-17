package com.bbva.enoa.platformservices.coreservice.common.repositories;

import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.datamodel.model.datastorage.entities.Filesystem;
import com.bbva.enoa.datamodel.model.datastorage.enumerates.FilesystemStatus;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentStatus;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

/**
 * Repository for HardwarePack.
 */
public interface FilesystemRepository extends JpaRepository<Filesystem, Integer>
{
    /**
     * Gets all filesystem of a product.
     *
     * @param productId {@link Product} ID
     * @return List of Filesystem.
     */
    List<Filesystem> findByProductIdOrderByCreationDateDesc(int productId);


    /**
     * Gets all filesystem of a product on an environment.
     *
     * @param productId   {@link Product} ID
     * @param environment {@link Environment}
     * @return List of Filesystem.
     */
    List<Filesystem> findByProductIdAndEnvironmentOrderByCreationDateDesc(int productId, String environment);

    @Query("select f from Filesystem f where type(f) = :filesystemType")
    <T extends Filesystem> List<Filesystem> findByFilesystemType(@Param("filesystemType") Class<T> filesystemType);

    @Query("select f from Filesystem f where f.product.uuaa = :uuaa and type(f) = :filesystemType")
    <T extends Filesystem> List<Filesystem> findByUUAAAndFilesystemType(@Param("uuaa") String uuaa, @Param("filesystemType") Class<T> filesystemType);

    @Query("select f from Filesystem f where f.environment = :environment and type(f) = :filesystemType")
    <T extends Filesystem> List<Filesystem> findByEnvironmentAndFilesystemType(@Param("environment") String environment, @Param("filesystemType") Class<T> filesystemType);

    @Query("select f from Filesystem f where f.product.uuaa = :uuaa and f.environment = :environment and type(f) = :filesystemType")
    <T extends Filesystem> List<Filesystem> findByEnvironmentAndUUAAAndFilesystemType(@Param("environment") String environment, @Param("uuaa") String uuaa, @Param("filesystemType") Class<T> filesystemType);

    @Query("select f from Filesystem f where f.product.id = :productId and type(f) = :filesystemType order by f.creationDate desc")
    <T extends Filesystem> List<Filesystem> findByProductIdAndFilesystemTypeOrderByCreationDateDesc(@Param("productId") int productId, @Param("filesystemType") Class<T> filesystemType);

    @Query("select f from Filesystem f where f.product.id = :productId and f.environment = :environment and type(f) = :filesystemType order by f.creationDate desc")
    <T extends Filesystem> List<Filesystem> findByProductIdAndEnvironmentAndFilesystemTypeOrderByCreationDateDesc(@Param("productId") int productId, @Param("environment") String environment, @Param("filesystemType") Class<T> filesystemType);

    /**
     * Gets the only {@link Filesystem} of a {@link Product}
     * of the same name on a given {@link Environment}.
     *
     * @param productId   {@link Product} ID
     * @param environment {@link Environment}
     * @param name        Name of the {@link Filesystem}
     * @return Filesystem
     */
    Filesystem findByProductIdAndEnvironmentAndNameOrderByCreationDateDesc(
            int productId,
            String environment,
            String name);

    /**
     * Checks if there is a {@link Filesystem} with the given name
     * in the {@link Product} on the same {@link Environment}.
     * <p>
     * Includes archived {@link Filesystem} since name must be unique always.
     *
     * @param productId      {@link Product} ID
     * @param filesystemName {@link Filesystem} name.
     * @param environment    {@link Environment}
     * @return List of Filesystem.
     */
    @Query(
            "select case when count ( f.id ) > 0 then true else false end " +
                    "from Filesystem f " +
                    "where " +
                    "   f.product.id = :productId " +
                    "and f.environment = :environment " +
                    "and f.name = :filesystemName " +
                    "and f.deletionDate is null ")
    boolean productHasFilesystemWithSameNameOnEnvironment(
            @Param("productId") int productId,
            @Param("filesystemName") String filesystemName,
            @Param("environment") String environment);


    /**
     * Checks if a {@link Filesystem} is being used on any {@link DeploymentService}
     * of a {@link Product} on a not undeployed {@link DeploymentPlan}.
     * <p>
     * Includes archived {@link Filesystem}.
     *
     * @param filesystemId {@link Filesystem} ID.
     * @return True if there is at least one service using the {@link Filesystem}.
     */
    @Query(
            "select case when count ( d.id ) > 0 then true else false end " +
                    "from DeploymentPlan d " +
                    "   join d.deploymentSubsystems subs " +
                    "   join subs.deploymentServices s " +
                    "   join s.deploymentServiceFilesystems dsf " +
                    "where " +
                    "   dsf.filesystem.id = :filesystemId " +
                    "and d.status != com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentStatus.UNDEPLOYED ")
    boolean filesystemIsUsed(@Param("filesystemId") int filesystemId);


    /**
     * Checks if a {@link Filesystem} is being used on any {@link DeploymentService}
     * of a {@link DeploymentPlan} in {@link DeploymentStatus}.DEPLOYED.
     *
     * @param filesystemId {@link Filesystem} ID.
     * @return True if there is at least one service using the {@link Filesystem}.
     */
    @Query(
            "select case when count ( d.id ) > 0 then true else false end " +
                    "from DeploymentPlan d " +
                    "   join d.deploymentSubsystems subs " +
                    "   join subs.deploymentServices s " +
                    "   join s.deploymentServiceFilesystems dsf " +
                    "where " +
                    "   dsf.filesystem.id = :filesystemId " +
                    "and d.status = com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentStatus.DEPLOYED ")
    boolean filesystemIsUsedOnDeployedPlan(@Param("filesystemId") int filesystemId);


    /**
     * Checks if a landing zone path is being used by any {@link Filesystem}
     * in a {@link Product} and {@link Environment}.
     * <p>
     * IMP: Excludes archived and failed {@link Filesystem} which could have the same landing zone..
     *
     * @param landingZonePath The path.
     * @param environment     The environment
     * @return True if it is being used.
     */
    @Query(
            "select case when count ( fs.id ) > 0 then true else false end " +
                    "from Filesystem fs " +
                    "where " +
                    " fs.landingZonePath = :landingZonePath " +
                    "and fs.environment = :environment " +
                    "and fs.filesystemStatus != com.bbva.enoa.datamodel.model.datastorage.enumerates.FilesystemStatus.ARCHIVED " +
                    "and fs.filesystemStatus != com.bbva.enoa.datamodel.model.datastorage.enumerates.FilesystemStatus.CREATE_ERROR " +
                    "and fs.filesystemStatus != com.bbva.enoa.datamodel.model.datastorage.enumerates.FilesystemStatus.DELETE_ERROR ")
    boolean landingZonePathIsUsed(@Param("landingZonePath") String landingZonePath, @Param("environment") String environment);

    /**
     * Gets all filesystem of a product.
     *
     * @param productId {@link Product} ID
     * @return List of Filesystem.
     */
    List<Filesystem> findByProductId(int productId);

    /**
     * Find filesystem by product id and status
     *
     * @param productId        product id
     * @param fileSystemStatus status
     * @return list of filesystem
     */
    List<Filesystem> findByProductIdAndFilesystemStatus(int productId, FilesystemStatus fileSystemStatus);

    /**
     * Get all DepploymentPlan where filesystem is being used.
     *
     * @param filesystemId filesystem ID.
     * @return list of plans.
     */
    @Query(
            "select distinct d " +
                    "from DeploymentPlan d " +
                    "join d.deploymentSubsystems subs " +
                    "join subs.deploymentServices s " +
                    "join s.deploymentServiceFilesystems dsf " +
                    "where dsf.filesystem.id = :filesystemId " +
                    "AND d.status in (com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentStatus.DEPLOYED, com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentStatus.DEFINITION )")
    List<DeploymentPlan> filesystemUsedOnDeployedPlan(@Param("filesystemId") int filesystemId);

    /**
     * Get filesystem id
     *
     * @param path         Landing zone path
     * @param environment  The environment of filesystem
     * @return id filesystem
     */
    @Query(
            "select f.id " +
                    "from Filesystem f " +
                    "where " +
                    "f.landingZonePath = :landingZonePath " +
                    "and f.environment = :environment ")
    Integer filesystemId(
            @Param("landingZonePath") String path,
            @Param("environment") String environment);

    /**
     * Find {@link Filesystem} by {@link FilesystemStatus}.
     *
     * @param status The given {@link FilesystemStatus}.
     * @return A List of Filesystem.
     */
    List<Filesystem> findByFilesystemStatus(FilesystemStatus status);

    /**
     * Find {@link Filesystem} by UUAA.
     *
     * @param uuaa The given UUAA.
     * @return A List of Filesystem.
     */
    List<Filesystem> findByProductUuaa(String uuaa);

    /**
     * Find {@link Filesystem} by UUAA and {@link FilesystemStatus}.
     *
     * @param uuaa      The given UUAA.
     * @param status    The given {@link FilesystemStatus}.
     * @return A List of Filesystem.
     */
    List<Filesystem> findByProductUuaaAndFilesystemStatus(String uuaa, FilesystemStatus status);

    /**
     * Find {@link Filesystem} by {@link Environment}.
     *
     * @param environment The given {@link Environment}.
     * @return A List of Filesystem.
     */
    List<Filesystem> findByEnvironment(String environment);

    /**
     * Find {@link Filesystem} by {@link Environment} and {@link FilesystemStatus}.
     *
     * @param environment   The given {@link Environment}.
     * @param status        The given {@link FilesystemStatus}.
     * @return A List of Filesystem.
     */
    List<Filesystem> findByEnvironmentAndFilesystemStatus(String environment, FilesystemStatus status);

    List<Filesystem> findByEnvironmentAndFilesystemStatusAndProductId(String environment, FilesystemStatus status, Integer productId);

    /**
     * Find {@link Filesystem} by {@link Environment} and UUAA.
     *
     * @param environment   The given {@link Environment}.
     * @param uuaa          The given UUAA.
     * @return A List of Filesystem.
     */
    List<Filesystem> findByEnvironmentAndProductUuaa(String environment, String uuaa);

    /**
     * Find {@link Filesystem} by {@link Environment}, UUAA and {@link FilesystemStatus}.
     *
     * @param environment   The given {@link Environment}.
     * @param uuaa          The given UUAA.
     * @param status        The given {@link FilesystemStatus}.
     * @return A List of Filesystem.
     */
    List<Filesystem> findByEnvironmentAndProductUuaaAndFilesystemStatus(String environment, String uuaa, FilesystemStatus status);

    List<Filesystem> findByIdIn(@Param("ids") Set<Integer> ids);
}
