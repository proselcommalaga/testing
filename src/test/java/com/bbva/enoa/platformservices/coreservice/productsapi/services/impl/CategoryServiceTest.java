package com.bbva.enoa.platformservices.coreservice.productsapi.services.impl;

import com.bbva.enoa.datamodel.model.product.entities.Category;
import com.bbva.enoa.datamodel.model.product.enumerates.ProductType;
import com.bbva.enoa.platformservices.coreservice.common.repositories.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.mockito.Mockito.*;

public class CategoryServiceTest
{
    @Mock
    private CategoryRepository productsApiCategoryRepository;
    @Spy
    @InjectMocks
    private CategoryService categoryService;

    @BeforeEach
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void getAllCategoriesTrueFunctionalTest()
    {
        List<Category> categoryList = this.generateCategoryList();
        when(this.productsApiCategoryRepository.findByProductType(anyString())).thenReturn(categoryList);
        this.categoryService.getAllCategories(ProductType.NOVA.getType(), true);

        verify(this.categoryService, atLeast(1)).isFunctionalCategory(anyString());
        verify(this.categoryService, atLeast(1)).isFunctionalCategory(anyString());

        assertArrayEquals(new Category[0], categoryList.toArray(new Category[0]));


    }

    @Test
    public void getAllCategoriesFalseFunctionalTest()
    {
        List<Category> categoryList = this.generateCategoryList();
        when(this.productsApiCategoryRepository.findByProductType(anyString())).thenReturn(categoryList);
        this.categoryService.getAllCategories(ProductType.NOVA.getType(), false);


        verify(this.categoryService, atLeast(1)).isFunctionalCategory(anyString());
        verify(this.categoryService, atLeast(1)).isFunctionalCategory(anyString());

        assertArrayEquals(new Category[]{categoryList.get(0), categoryList.get(1)}, categoryList.toArray(new Category[0]));

    }

    @Test
    public void getAllCategoriesNullFunctionalTest()
    {
        List<Category> categoryList = this.generateCategoryList();
        when(this.productsApiCategoryRepository.findByProductType(anyString())).thenReturn(categoryList);
        this.categoryService.getAllCategories(ProductType.NOVA.getType(), null);


        verify(this.categoryService, times(0)).isFunctionalCategory(anyString());
        verify(this.categoryService, times(0)).isFunctionalCategory(anyString());

        assertArrayEquals(new Category[]{categoryList.get(0), categoryList.get(1)}, categoryList.toArray(new Category[0]));

    }


    @Test
    public void removeEmptyCategoriesAtLeastOneRemoveTest()
    {

        List<Category> categoryList = this.generateCategoryList();
        when(productsApiCategoryRepository.findAll()).thenReturn(categoryList);
        when(productsApiCategoryRepository.countProductsAssigned(anyInt())).thenReturn(1L);

        this.categoryService.removeEmptyCategories();
        verify(this.productsApiCategoryRepository, times(0)).deleteAll(any());
    }

    @Test
    public void removeEmptyCategoriesNoOneTest()
    {

        List<Category> categoryList = this.generateCategoryList();
        when(productsApiCategoryRepository.findAll()).thenReturn(categoryList);

        //Only one category matches
        when(productsApiCategoryRepository.countProductsAssigned(categoryList.get(0).getId())).thenReturn(1L);
        when(productsApiCategoryRepository.countProductsAssigned(categoryList.get(1).getId())).thenReturn(0L);

        this.categoryService.removeEmptyCategories();
        verify(this.productsApiCategoryRepository, times(1)).deleteAll(any());
    }

    private List<Category> generateCategoryList()
    {

        List<Category> categoryList = new ArrayList<>();

        categoryList.add(new Category("typedCategory1"));
        categoryList.get(0).setId(1);
        categoryList.add(new Category("typedCategory2"));
        categoryList.get(1).setId(2);
        return categoryList;

    }

}