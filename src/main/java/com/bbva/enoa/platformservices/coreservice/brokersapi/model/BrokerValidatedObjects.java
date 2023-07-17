package com.bbva.enoa.platformservices.coreservice.brokersapi.model;

import com.bbva.enoa.datamodel.model.datastorage.entities.Filesystem;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.resource.entities.BrokerPack;
import lombok.Data;

/**
 * POJO to encapsulate all objets that have been validated and are needed after validations to create the broker
 */
@Data
public class BrokerValidatedObjects
{
    private final Product product;
    private final Filesystem filesystem;
    private final BrokerPack hardwarePack;

    public BrokerValidatedObjects(final Product product, final Filesystem filesystem, final BrokerPack hardwarePack)
    {
        this.product = product;
        this.filesystem = filesystem;
        this.hardwarePack = hardwarePack;
    }
}
