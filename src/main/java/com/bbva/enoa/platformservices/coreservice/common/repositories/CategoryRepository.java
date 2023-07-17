package com.bbva.enoa.platformservices.coreservice.common.repositories;

import com.bbva.enoa.datamodel.model.product.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Category repository.
 */
@Transactional(readOnly = true)
public interface CategoryRepository extends JpaRepository<Category, Integer>
{
    /**
     * This method returns the category corresponding to a given name
     *
     * @param name category name
     * @return category entity
     */
    Category findByName(@Param("name") String name);

    /**
     * Gets categories by product type
     *
     * @param type type of product (ALL, NOVA or LIBRARY)
     * @return - Categories ordered by name
     */
    @Query(" select distinct c from Product p " +
            " join p.categories c " +
            " where " +
            " 'ALL' = :type or p.type = :type " +
            " order by c.name asc")
    List<Category> findByProductType(@Param("type") String type);


    /**
     * Gets the number of products of the given category.
     *
     * @param categoryId ID
     * @return Number of products of the category.
     */
    @Query(" select count (*) " +
            " from Product p " +
            " join p.categories c " +
            " where " +
            " c.id = :categoryId")
    long countProductsAssigned(@Param("categoryId") Integer categoryId);
}
