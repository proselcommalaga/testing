package com.bbva.enoa.platformservices.coreservice.productsapi.services.interfaces;

/**
 * Services for end point categories
 * Created by BBVA - xe30432 on 02/02/2017.
 */
public interface ICategoryService
{
    /**
     * Recovers all the product categories
     *
     * @param type          filter could be NOVA or LIBRARY
     * @param functional    If it's true, filter by categories considered functional.
     *                      If it's false, filter by categories considered not functional.
     *                      If it's not present (or empty), don't filter.
     * @return the array of categories
     */
    String[] getAllCategories(String type, Boolean functional);

    /**
     * Removes the categories without products
     */
    void removeEmptyCategories();

    /**
     * Whether the name of a category is considered of "type" functional.
     *
     * @param categoryName The name of the Category.
     * @return Whether the name of a category is considered of "type" functional.
     */
    boolean isFunctionalCategory(String categoryName);
}
