package com.bbva.enoa.platformservices.coreservice.common.repositories;


import com.bbva.enoa.platformservices.coreservice.common.view.ProductCSV;

import java.util.List;

/**
 * Created by xe69190 on 12/01/2018.
 */
public interface ProductCustomRepository {


    /**
     * Query of all Products and Subsystem and Services for report purpose
     *
     * @return List   Products and Subsystem and Services
     */
    List<ProductCSV> findProductsCsv();
}
