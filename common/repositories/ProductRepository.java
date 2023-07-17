package com.bbva.enoa.platformservices.coreservice.common.repositories;

import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.product.enumerates.ProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Product repository.
 */
@Transactional(readOnly = true)
public interface ProductRepository extends JpaRepository<Product, Integer>
{

    /**
     * Find a product by its UUAA.
     *
     * @param uuaa Product UUAA.
     * @return Product.
     */
    Product findOneByUuaa(String uuaa);

    /**
     * Find a product by its UUAA (case insensitive).
     *
     * @param uuaa Product UUAA.
     * @return Product.
     */
    Optional<Product> findOneByUuaaIgnoreCase(String uuaa);

    /**
     * Find a product id by its UUAA.
     *
     * @param uuaa Product UUAA.
     * @return Product id.
     */
    @Query(
            "select id " +
                    "from Product p " +
                    "where p.uuaa = :uuaa")
    Integer findProductIdByUuaa(@Param("uuaa") final String uuaa);

    /**
     * Get a product list filter by status and type
     *
     * @param status the uuaa name to apply the filter
     * @param type   filter could be NOVA or LIBRARY
     * @return a product list with the same uuaa
     */
    @Query("select p"
            + " from Product p"
            + " where"
            + " p.productStatus = :statusCode and ('ALL' = :type or p.type = :type)")
    List<Product> findByProductStatusType(@Param("statusCode") final ProductStatus status, @Param("type") final String type);

    /**
     * Gets a product by name
     *
     * @param name unique name of the product
     * @return the product
     */
    @Query(
            "select p from Product p" +
                    " where upper( p.name )= :name")
    Product findByName(@Param("name") final String name);

    /**
     * Get a product list filter by uuaa
     *
     * @param uuaa the uuaa name to apply the filter
     * @return a product list with the same uuaa
     */
    Product findByUuaaIgnoreCase(@Param("uuaa") final String uuaa);

    /**
     * Gets a product by id
     *
     * @param id of the product
     * @return the product
     */
    @Query("select p from Product p" +
            " where p.id = :id")
    Product fetchById(@Param("id") final Integer id);

    /**
     * Get a product list filter by uuaa
     *
     * @param uuaa the uuaa name to apply the filter
     * @return a product list with the same uuaa
     */
    List<Product> findByUuaa(@Param("uuaa") final String uuaa);


    /**
     * Get a product  filter by desBoard (JIRA KEY)
     *
     * @param jiraKey the uuaa name to apply the filter
     * @return a product with the same desBoard
     */
    Product findByDesBoard(@Param("desBoard") final String jiraKey);


    /**
     * Get a product list filter by type in given list
     *
     * @param productList list of products
     * @param type        filter could be NOVA or LIBRARY
     * @return a product list with the same uuaa
     */
    @Query("select p"
            + " from Product p"
            + " where"
            + " p.id in :productList and ('ALL' = :type or p.type = :type)")
    List<Product> findByIdsType(@Param("productList") final int[] productList, @Param("type") final String type);

    /**
     * Get all products ordered by UAAA ascending.
     *
     * @return A List of all Product, ordered by UAAA ascending.
     */
    List<Product> findAllByOrderByUuaaAsc();

    /**
     * Get the products in the Category with the given name.
     *
     * @param categoryName The Name of the Category.
     * @return A List of Product.
     */
    List<Product> findDistinctByCategoriesName(String categoryName);

    /**
     * Get the products in the Category with the given name.
     *
     * @param categoriesNames The Names of the Categories.
     * @return A List of Product.
     */
    List<Product> findDistinctByCategoriesNameIn(List<String> categoriesNames);

    /**
     * Gets an array of product ids related to a given uuaa.
     *
     * @param uuaa The uuaa assigned to a product.
     * @return An array of product ids related to a given uuaa.
     */
    @Query(value = "select p.id from product p where coalesce(cast(:uuaa as text), p.uuaa) = p.uuaa", nativeQuery = true)
    Long[] findProductIdsByUuaa(final String uuaa);

    /**
     * Get type ordered by Type ascending.
     *
     * @return A List of types, ordered by Type ascending.
     */
    List<Product> findAllByOrderByTypeAsc();

    /**
     * Return a list of all existing duples (product_id, uuaa)
     *
     * @return a list of all existing duples (product_id, uuaa)
     */
    @Query(value = "select p.id, p.uuaa from product p", nativeQuery = true)
    List<Object[]> findIdUuaaPairs();

    @Query(value = "select p.uuaa, string_agg(distinct dp.environment, ',') " +
            "from deployment_subsystem ds " +
            "inner join release_version_subsystem rvs on ds.subsystem_id = rvs.id " +
            "inner join release_version rv on rvs.release_version_id = rv.id " +
            "inner join \"release\" r on rv.release_id = r.id " +
            "inner join product p on r.product_id = p.id " +
            "inner join deployment_plan dp on ds.deployment_plan_id = dp.id " +
            "where dp.status = 'DEPLOYED' " +
            "group by p.uuaa", nativeQuery = true)
    List<Object[]> getProductUuaaEnvironments();

    /**
     * Find the Product Id which the given Release Version Service Id belongs to
     *
     * @param releaseVersionServiceId The Id of the Release Version Service
     * @return The Id of the Product which the given Release Version Service Id belongs to
     */
    @Query(value = " select p.id " +
            " from product p " +
            " inner join \"release\" r on p.id = r.product_id " +
            " inner join release_version rv on r.id = rv.release_id " +
            " inner join release_version_subsystem rvss on rv.id = rvss.release_version_id " +
            " inner join release_version_service rvs on rvss.id = rvs.version_subsystem_id " +
            " where rvs.id = :releaseVersionServiceId ", nativeQuery = true)
    Integer findProductIdByReleaseVersionServiceId(@Param("releaseVersionServiceId") final Integer releaseVersionServiceId);

    /**
     * Find the Product Id which the given Behavior Version Service Id belongs to
     *
     * @param behaviorVersionServiceId The Id of the Behavior Version Service
     * @return The Id of the Product which the given Behavior Version Service Id belongs to
     */
    @Query(value = " select p.id " +
            " from product p " +
            " inner join behavior_version bv on p.id = bv.product_id " +
            " inner join behavior_subsystem bss on bv.id = bss.behavior_version_id " +
            " inner join behavior_service bs on bss.id = bs.behavior_subsystem_id " +
            " where bs.id = :behaviorVersionServiceId ", nativeQuery = true)
    Integer findProductIdByBehaviorVersionServiceId(@Param("behaviorVersionServiceId") final Integer behaviorVersionServiceId);

    /**
     * Find the Product Id which the given Release Version Id belongs to
     *
     * @param releaseVersionId The Id of the Release Version
     * @return The Id of the Product which the given Release Version Id belongs to
     */
    @Query(value = " select p.id " +
            " from product p " +
            " inner join \"release\" r on p.id = r.product_id " +
            " inner join release_version rv on r.id = rv.release_id " +
            " where rv.id = :releaseVersionId ", nativeQuery = true)
    Integer findProductIdByReleaseVersionId(@Param("releaseVersionId") final Integer releaseVersionId);

    /**
     * Get UUAA, environment and deployed platform list. One product is deployed in ETHER platform in case of almost one
     * deployment plan in the same environment deployed in ETHER in other case deployed platform is NOVA
     *
     * @return a UUAA environment and deployed platform list.
     */
    @Query(value = "select " +
            "  case " +
            "    when count (distinct dp.selected_deploy) > 1 then 'ETHER' " +
            "    else 'NOVA' " +
            "  end as deploy" +
            "  , dp.environment, p.uuaa " +
            "from product p " +
            "inner join  \"release\" r on p.id = r.product_id " +
            "inner join release_version rv on r.id = rv.release_id " +
            "inner join deployment_plan dp on rv.id = dp.release_version_id " +
            "group by p.uuaa, dp.environment " +
            "order by deploy, dp.environment, p.uuaa", nativeQuery = true)
    List<Object[]> findProductsByEnvironmentAndDeployedPlatform();

    /**
     * Get a products by productId
     *
     * @param productid list of products
     * @return a product list with the same uuaa
     */
    @Query("select p"
            + " from Product p"
            + " where"
            + " p.id in :productid")
    List<Product> findAllById(int[] productid);
}