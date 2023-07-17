package com.bbva.enoa.platformservices.coreservice.productsapi.util;


import com.bbva.enoa.datamodel.model.product.entities.Category;
import com.bbva.enoa.platformservices.coreservice.common.repositories.CategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Services for end point categories
 * Created by BBVA - xe30432 on 02/02/2017.
 */
public final class CategoryUtils
{
    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(CategoryUtils.class);

    /**
     * Empty constructor
     */
    private CategoryUtils()
    {
        //Empty constructor
    }

    /**
     * Find for each categories of the categoryStringList. If some category exists, insert a new one into the data base.
     * Return a new Category list with the same categories
     *
     * @param categoryStringList            categories for find into the BBDD
     * @param productsApiCategoryRepository productsApiProductRepository
     * @return a categories list
     */
    public static List<Category> findAndInsertCategory(final String[] categoryStringList,
                                                       final CategoryRepository productsApiCategoryRepository)
    {
        LOG.debug("ProductsAPI: /products/create-> find the follows products categories [{}] into BBDD", categoryStringList);

        // Iterate for each category and create a new one category in case of not exists
        List<Category> categoryList = new ArrayList<>();
        for (String category : categoryStringList)
        {
            // Create the category instance
            Category categoryEntity = productsApiCategoryRepository.findByName(category);

            // Check if this category exists into BBDD
            if (categoryEntity == null)
            {
                LOG.debug("ProductsAPI: /products/create -> This category [{}] does not exist into BBDD", category);
                categoryEntity = new Category(category);

                // Save the category into BBDD
                productsApiCategoryRepository.save(categoryEntity);
                LOG.debug("ProductsAPI: /products/create -> The category [{}] has been adedd into BBDD.", categoryEntity);
            }

            // Add into the product any case into categoryList
            categoryList.add(categoryEntity);
        }

        LOG.debug("ProductsAPI: /products/create-> associated this categories [{}] for the product.", categoryList);
        return categoryList;
    }


}
