package com.bbva.enoa.platformservices.coreservice.common.repositories;

import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion;
import com.bbva.enoa.datamodel.model.release.enumerates.ReleaseVersionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Release version repository.
 */
public interface ReleaseVersionRepository extends JpaRepository<ReleaseVersion, Integer>
{
    /**
     * Get release version for status
     *
     * @param releaseVersionStatus the release version status
     * @return release version list
     */
    List<ReleaseVersion> findByStatus(ReleaseVersionStatus releaseVersionStatus);

    /**
     * Get release version for subsystem
     *
     * @param subsystemId subsystem id
     * @return release version
     */
    @Query(
            " select t " +
                    " from ReleaseVersion t" +
                    " join t.subsystems s " +
                    " where " +
                    " s.id = :subsystemId")
    ReleaseVersion releaseVersionOfSubsystem(@Param("subsystemId") final Integer subsystemId);

    /**
     * Count number of image names
     *
     * @param releaseId release id
     * @param imageName image name
     * @return number of image names
     */
    @Query(
            "select count(*) from ReleaseVersion rv" +
                    " join rv.subsystems su " +
                    " join su.services se " +
                    " where " +
                    " rv.release.id = :releaseId " +
                    " AND se.imageName = :imageName" +
                    " AND rv.status != com.bbva.enoa.datamodel.model.release.enumerates.ReleaseVersionStatus.STORAGED"
    )
    long countImageName(@Param("releaseId") final Integer releaseId, @Param("imageName") final String imageName);

    /**
     * Count number of release versions by product id an not with status
     *
     * @param productId Product to count release versions for
     * @param storaged  Status to exclude from count
     * @return Number of release versions found
     */
    @Query("select count(v.id) from ReleaseVersion v where v.release.product.id = :productId and v.status <> :status")
    int countByProductIdAndStatusNot(@Param("productId") final Integer productId,
                                     @Param("status") final ReleaseVersionStatus storaged);

    /**
     * Count by productId and status
     *
     * @param productId product id
     * @param storaged  status
     * @return a count by product id and status
     */
    @Query("select count(v.id) from ReleaseVersion v where v.release.product.id = :productId and v.status = :status")
    int countByProductIdAndStatus(@Param("productId") final Integer productId,
                                  @Param("status") final ReleaseVersionStatus storaged);

    /**
     * Find by product and version name
     *
     * @param productId   product id
     * @param versionName version name
     * @return ReleaseVersion
     */
    @Query("select v from ReleaseVersion v where v.release.product.id = :productId and v.versionName = :versionName")
    ReleaseVersion findByProductIdAndVersionName(@Param("productId") final Integer productId,
                                                 @Param("versionName") final String versionName);

    /**
     * Count ephoenix services
     *
     * @param releaseVersionId release version id
     * @return count
     */
    @Query(
            "select count(*) from ReleaseVersion rv" +
                    " join rv.subsystems su " +
                    " join su.services se " +
                    " where " +
                    " rv.id = :releaseVersionId " +
                    " AND ( " +
                    " se.serviceType = 'EPHOENIX_BATCH' " +
                    " OR " +
                    " se.serviceType = 'EPHOENIX_ONLINE' " +
                    " )"
    )
    long countPhoenixServices(@Param("releaseVersionId") final Integer releaseVersionId);

    /**
     * Returns a tuple (service_type, number_of_services_of_given_type, number_of_releases_for_given_service) from
     * release version model. It's necessary for populating statistics service response.
     *
     * @param uuaa                 The product uuaa
     * @param releaseVersionStatus The release version status
     * @return A list of tuples (service_type, number_of_services_of_given_type, number_of_releases_for_given_service)
     */
    @Query(value = "select rvs2.service_type " +
            ",count(1) as services " +
            ",(select count(1) over () " +
            "from release_version rv  " +
            "inner join \"release\" r2  " +
            "on r2.id = rv.release_id  " +
            "inner join product p2  " +
            "on p2.id = r2.product_id  " +
            "inner join release_version_subsystem rvs  " +
            "on rv.id = rvs.release_version_id  " +
            "inner join release_version_service rvs2  " +
            "on rvs.id = rvs2.version_subsystem_id  " +
            "where p2.uuaa = upper(:uuaa) " +
            "and rv.status = :releaseVersionStatus " +
            "and ((r2.selected_deploy_int = upper(:platform)) " +
            "or (r2.selected_deploy_pre = upper(:platform)) " +
            "or (r2.selected_deploy_pro = upper(:platform))) " +
            "group by rv.id limit 1) as release_services " +
            "from release_version rv  " +
            "inner join \"release\" r2  " +
            "on r2.id = rv.release_id  " +
            "inner join product p2  " +
            "on p2.id = r2.product_id  " +
            "inner join release_version_subsystem rvs  " +
            "on rv.id = rvs.release_version_id  " +
            "inner join release_version_service rvs2  " +
            "on rvs.id = rvs2.version_subsystem_id  " +
            "where p2.uuaa = upper(:uuaa) " +
            "and rv.status = :releaseVersionStatus " +
            "and ((r2.selected_deploy_int = upper(:platform)) " +
            "or (r2.selected_deploy_pre = upper(:platform)) " +
            "or (r2.selected_deploy_pro = upper(:platform))) " +
            "group by rvs2.service_type ", nativeQuery = true)
    List<Object[]> findAllByUuaaAndReleaseVersionStatusAndDeployPlatform(String uuaa, String releaseVersionStatus, String platform);

    /**
     * Returns a tuple (service_type, number_of_services_of_given_type, number_of_releases_for_given_service) from
     * release version model. It's necessary for populating statistics service response.
     *
     * @param uuaa                 The product uuaa
     * @param releaseVersionStatus The release version status
     * @return A list of tuples (service_type, number_of_services_of_given_type, number_of_releases_for_given_service)
     */
    @Query(value = "select rvs2.service_type " +
            ",count(1) as services " +
            ",(select count(1) over () " +
            "from release_version rv  " +
            "inner join \"release\" r2  " +
            "on r2.id = rv.release_id  " +
            "inner join product p2  " +
            "on p2.id = r2.product_id  " +
            "inner join release_version_subsystem rvs  " +
            "on rv.id = rvs.release_version_id  " +
            "inner join release_version_service rvs2  " +
            "on rvs.id = rvs2.version_subsystem_id  " +
            "where p2.uuaa = upper(:uuaa) " +
            "and rv.status = :releaseVersionStatus " +
            "group by rv.id limit 1) as release_services " +
            "from release_version rv  " +
            "inner join \"release\" r2  " +
            "on r2.id = rv.release_id  " +
            "inner join product p2  " +
            "on p2.id = r2.product_id  " +
            "inner join release_version_subsystem rvs  " +
            "on rv.id = rvs.release_version_id  " +
            "inner join release_version_service rvs2  " +
            "on rvs.id = rvs2.version_subsystem_id  " +
            "where p2.uuaa = upper(:uuaa) " +
            "and rv.status = :releaseVersionStatus " +
            "group by rvs2.service_type ", nativeQuery = true)
    List<Object[]> findAllByUuaaAndReleaseVersionStatus(String uuaa, String releaseVersionStatus);

    /**
     * Returns a tuple (service_type, number_of_services_of_given_type, number_of_releases_for_given_service) from
     * release version model. It's necessary for populating statistics service response.
     *
     * @param uuaa     The product uuaa
     * @param platform The release version platform
     * @return A list of tuples (service_type, number_of_services_of_given_type, number_of_releases_for_given_service)
     */
    @Query(value = "select rvs2.service_type " +
            ",count(1) as services " +
            ",(select count(1) over () " +
            "from release_version rv  " +
            "inner join \"release\" r2  " +
            "on r2.id = rv.release_id  " +
            "inner join product p2  " +
            "on p2.id = r2.product_id  " +
            "inner join release_version_subsystem rvs  " +
            "on rv.id = rvs.release_version_id  " +
            "inner join release_version_service rvs2  " +
            "on rvs.id = rvs2.version_subsystem_id  " +
            "where p2.uuaa = upper(:uuaa)" +
            "and ((r2.selected_deploy_int = upper(:platform)) " +
            "or (r2.selected_deploy_pre = upper(:platform)) " +
            "or (r2.selected_deploy_pro = upper(:platform))) " +
            "group by rv.id limit 1) as release_services\n" +
            "from release_version rv  " +
            "inner join \"release\" r2  " +
            "on r2.id = rv.release_id  " +
            "inner join product p2  " +
            "on p2.id = r2.product_id  " +
            "inner join release_version_subsystem rvs  " +
            "on rv.id = rvs.release_version_id  " +
            "inner join release_version_service rvs2  " +
            "on rvs.id = rvs2.version_subsystem_id  " +
            "where p2.uuaa = upper(:uuaa) " +
            "and ((r2.selected_deploy_int = upper(:platform)) " +
            "or (r2.selected_deploy_pre = upper(:platform)) " +
            "or (r2.selected_deploy_pro = upper(:platform))) " +
            "group by rvs2.service_type ", nativeQuery = true)
    List<Object[]> findAllByUuaaAndDeployPlatform(String uuaa, String platform);

    /**
     * Returns a tuple (service_type, number_of_services_of_given_type, number_of_releases_for_given_service) from
     * release version model. It's necessary for populating statistics service response.
     *
     * @param releaseVersionStatus The product uuaa
     * @param platform             The release version platform
     * @return A list of tuples (service_type, number_of_services_of_given_type, number_of_releases_for_given_service)
     */
    @Query(value = "select rvs2.service_type " +
            ",count(1) as services " +
            ",(select count(1) over () " +
            "from release_version rv  " +
            "inner join \"release\" r2  " +
            "on r2.id = rv.release_id  " +
            "inner join product p2  " +
            "on p2.id = r2.product_id  " +
            "inner join release_version_subsystem rvs  " +
            "on rv.id = rvs.release_version_id  " +
            "inner join release_version_service rvs2  " +
            "on rvs.id = rvs2.version_subsystem_id  " +
            "where rv.status = :releaseVersionStatus " +
            "and ((r2.selected_deploy_int = upper(:platform)) " +
            "or (r2.selected_deploy_pre = upper(:platform)) " +
            "or (r2.selected_deploy_pro = upper(:platform))) " +
            "group by rv.id limit 1) as release_services " +
            "from release_version rv  " +
            "inner join \"release\" r2  " +
            "on r2.id = rv.release_id  " +
            "inner join product p2  " +
            "on p2.id = r2.product_id  " +
            "inner join release_version_subsystem rvs  " +
            "on rv.id = rvs.release_version_id  " +
            "inner join release_version_service rvs2  " +
            "on rvs.id = rvs2.version_subsystem_id  " +
            "where rv.status = :releaseVersionStatus " +
            "and ((r2.selected_deploy_int = upper(:platform)) " +
            "or (r2.selected_deploy_pre = upper(:platform)) " +
            "or (r2.selected_deploy_pro = upper(:platform))) " +
            "group by rvs2.service_type ", nativeQuery = true)
    List<Object[]> findAllByReleaseVersionStatusAndDeployPlatform(String releaseVersionStatus, String platform);


    /**
     * Returns a tuple (service_type, number_of_services_of_given_type, number_of_releases_for_given_service) from
     * release version model. Release versions with STORAGED status are ignored. It's necessary for populating
     * statistics service response.
     *
     * @param uuaa The product uuaa
     * @return A list of tuples (service_type, number_of_services_of_given_type, number_of_releases_for_given_service)
     */
    @Query(value = "select rvs2.service_type " +
            ",count(1) as services " +
            ",(select count(1) over () " +
            "from release_version rv  " +
            "inner join \"release\" r2  " +
            "on r2.id = rv.release_id  " +
            "inner join product p2  " +
            "on p2.id = r2.product_id  " +
            "inner join release_version_subsystem rvs  " +
            "on rv.id = rvs.release_version_id  " +
            "inner join release_version_service rvs2  " +
            "on rvs.id = rvs2.version_subsystem_id  " +
            "where p2.uuaa = upper(:uuaa) " +
            "and rv.status <> 'STORAGED' " +
            "group by rv.id limit 1) as release_services " +
            "from release_version rv  " +
            "inner join \"release\" r2  " +
            "on r2.id = rv.release_id  " +
            "inner join product p2  " +
            "on p2.id = r2.product_id  " +
            "inner join release_version_subsystem rvs  " +
            "on rv.id = rvs.release_version_id  " +
            "inner join release_version_service rvs2  " +
            "on rvs.id = rvs2.version_subsystem_id  " +
            "where p2.uuaa = upper(:uuaa) " +
            "and rv.status <> 'STORAGED' " +
            "group by rvs2.service_type ", nativeQuery = true)
    List<Object[]> findNotStoragedByUuaa(String uuaa);

    /**
     * Returns a tuple (service_type, number_of_services_of_given_type, number_of_releases_for_given_service) from
     * release version model. It's necessary for populating statistics service response.
     *
     * @param releaseVersionStatus The release version status
     * @return A list of tuples (service_type, number_of_services_of_given_type, number_of_releases_for_given_service)
     */
    @Query(value = "select rvs2.service_type " +
            ",count(1) as services " +
            ",(select count(1) over () " +
            "from release_version rv  " +
            "inner join \"release\" r2  " +
            "on r2.id = rv.release_id  " +
            "inner join product p2  " +
            "on p2.id = r2.product_id  " +
            "inner join release_version_subsystem rvs  " +
            "on rv.id = rvs.release_version_id  " +
            "inner join release_version_service rvs2  " +
            "on rvs.id = rvs2.version_subsystem_id  " +
            "where rv.status = :releaseVersionStatus " +
            "group by rv.id limit 1) as release_services " +
            "from release_version rv  " +
            "inner join \"release\" r2  " +
            "on r2.id = rv.release_id  " +
            "inner join product p2  " +
            "on p2.id = r2.product_id  " +
            "inner join release_version_subsystem rvs  " +
            "on rv.id = rvs.release_version_id  " +
            "inner join release_version_service rvs2  " +
            "on rvs.id = rvs2.version_subsystem_id  " +
            "where rv.status = :releaseVersionStatus " +
            "group by rvs2.service_type ", nativeQuery = true)
    List<Object[]> findByReleaseVersionStatus(String releaseVersionStatus);

    /**
     * Returns a tuple (service_type, number_of_services_of_given_type, number_of_releases_for_given_service) from
     * release version model. It's necessary for populating statistics service response.
     *
     * @param platform The release version status
     * @return A list of tuples (service_type, number_of_services_of_given_type, number_of_releases_for_given_service)
     */
    @Query(value = "select rvs2.service_type " +
            ",count(1) as services " +
            ",(select count(1) over () " +
            "from release_version rv  " +
            "inner join \"release\" r2  " +
            "on r2.id = rv.release_id  " +
            "inner join product p2  " +
            "on p2.id = r2.product_id  " +
            "inner join release_version_subsystem rvs  " +
            "on rv.id = rvs.release_version_id  " +
            "inner join release_version_service rvs2  " +
            "on rvs.id = rvs2.version_subsystem_id  " +
            "where ((r2.selected_deploy_int = upper(:platform)) " +
            "or (r2.selected_deploy_pre = upper(:platform)) " +
            "or (r2.selected_deploy_pro = upper(:platform))) " +
            "and rv.status <> 'STORAGED'" +
            "group by rv.id limit 1) as release_services " +
            "from release_version rv  " +
            "inner join \"release\" r2  " +
            "on r2.id = rv.release_id  " +
            "inner join product p2  " +
            "on p2.id = r2.product_id  " +
            "inner join release_version_subsystem rvs  " +
            "on rv.id = rvs.release_version_id  " +
            "inner join release_version_service rvs2  " +
            "on rvs.id = rvs2.version_subsystem_id  " +
            "where ((r2.selected_deploy_int = upper(:platform)) " +
            "or (r2.selected_deploy_pre = upper(:platform)) " +
            "or (r2.selected_deploy_pro = upper(:platform))) " +
            "group by rvs2.service_type ", nativeQuery = true)
    List<Object[]> findByPlatform(String platform);

    /**
     * Returns a tuple (service_type, number_of_services_of_given_type, number_of_releases_for_given_service) from
     * release version model. Release versions with STORAGED status are ignored. It's necessary for populating
     * statistics service response.
     *
     * @return A list of tuples (service_type, number_of_services_of_given_type, number_of_releases_for_given_service)
     */
    @Query(value = "select rvs2.service_type " +
            ",count(1) as services " +
            ",(select count(1) over () " +
            "from release_version rv  " +
            "inner join \"release\" r2  " +
            "on r2.id = rv.release_id  " +
            "inner join product p2  " +
            "on p2.id = r2.product_id  " +
            "inner join release_version_subsystem rvs  " +
            "on rv.id = rvs.release_version_id  " +
            "inner join release_version_service rvs2  " +
            "on rvs.id = rvs2.version_subsystem_id  " +
            "where rv.status <> 'STORAGED' " +
            "group by rv.id limit 1) as release_services " +
            "from release_version rv  " +
            "inner join \"release\" r2  " +
            "on r2.id = rv.release_id  " +
            "inner join product p2  " +
            "on p2.id = r2.product_id  " +
            "inner join release_version_subsystem rvs  " +
            "on rv.id = rvs.release_version_id  " +
            "inner join release_version_service rvs2  " +
            "on rvs.id = rvs2.version_subsystem_id  " +
            "where rv.status <> 'STORAGED' " +
            "group by rvs2.service_type ", nativeQuery = true)
    List<Object[]> findAllNotStoragedElements();


    @Query(value = "select rv.* " +
            "from release_version rv " +
            "join release r on rv.release_id = r.id " +
            "join product p on r.product_id = p.id " +
            "join deployment_plan dp on rv.id = dp.release_version_id " +
            "where dp.status = 'DEPLOYED' and dp.environment = 'PRE' and p.id = :productId", nativeQuery = true)
    List<ReleaseVersion> findAllBehaviorReleasesDeployedInPre(Integer productId);

}