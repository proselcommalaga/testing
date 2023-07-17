package com.bbva.enoa.platformservices.coreservice.productsapi.util;

import com.bbva.enoa.datamodel.model.product.entities.Category;
import com.bbva.enoa.platformservices.coreservice.common.repositories.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class CategoryUtilsTest
{
    @Mock
    private CategoryRepository productsApiCategoryRepository;

    @BeforeEach
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void findAndInsertCategory()
    {
        String[] categories = new String[]{"CAT"};
        List<Category> response = CategoryUtils.findAndInsertCategory(categories, this.productsApiCategoryRepository);
        assertEquals(1, response.size());
    }
}