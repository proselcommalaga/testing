package com.bbva.enoa.platformservices.coreservice.productsapi.services.impl;

import com.bbva.enoa.datamodel.model.product.entities.Category;
import com.bbva.enoa.datamodel.model.product.enumerates.ProductType;
import com.bbva.enoa.platformservices.coreservice.common.repositories.CategoryRepository;
import com.bbva.enoa.platformservices.coreservice.productsapi.util.Constants;
import com.bbva.enoa.platformservices.coreservice.productsapi.util.ProductConverter;
import com.bbva.enoa.utils.clientsutils.services.interfaces.IErrorTaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for product categories
 */
@Service
public class CategoryService implements com.bbva.enoa.platformservices.coreservice.productsapi.services.interfaces.ICategoryService
{
    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(CategoryService.class);

    @Autowired
    private CategoryRepository productsApiCategoryRepository;

    @Autowired
    private IErrorTaskManager errorTaskMgr;

    @Override
    public String[] getAllCategories(String type, Boolean functional)
    {
        List<Category> categoryArrayList = this.validateCategories(type);

        if (functional != null)
        {
            if (functional)
            {
                categoryArrayList.removeIf(category -> !isFunctionalCategory(category.getName()));
            }
            else
            {
                categoryArrayList.removeIf(category -> isFunctionalCategory(category.getName()));
            }
        }

        // Convert and get the category entity to category string array
        return ProductConverter.convertCategoryEntityListToStringList(categoryArrayList);
    }

    /**
     * Validates the categories
     *
     * @param type filter by type
     * @return a categories list from BBDD
     */
    private List<Category> validateCategories(final String type)
    {
        LOG.debug("[ProductsAPI] -> [validateCategories]: getting categories from BBDD for type: [{}]", type);
        return productsApiCategoryRepository.findByProductType(ProductType.getValueOf(type).toString());
    }


    /**
     * Remove categories without products assigned.
     */
    @Override
    public void removeEmptyCategories()
    {
        List<Category> categories = productsApiCategoryRepository.findAll();
        List<Category> toRemove = new ArrayList();
        // For each category
        for (Category cat : categories)
        {
            this.checkAssociatedProducts(toRemove, cat);
        }
        this.removeIfNotEmpty(toRemove);
    }

    @Override
    public boolean isFunctionalCategory(String categoryName)
    {
        return categoryName != null && categoryName.matches(Constants.FUNCTIONAL_CATEGORIES_REGEX);
    }

    private void removeIfNotEmpty(List<Category> toRemove)
    {
        if (!toRemove.isEmpty())
        {
            LOG.debug("Removing {} empty Categories ...", toRemove.size());
            productsApiCategoryRepository.deleteAll(toRemove);
        }
    }

    private void checkAssociatedProducts(List<Category> toRemove, Category cat)
    {
        if (productsApiCategoryRepository.countProductsAssigned(cat.getId()) == 0)
        {
            LOG.debug("Category {} without assigned products is going to be removed", cat);
            toRemove.add(cat);
        }
    }

}
