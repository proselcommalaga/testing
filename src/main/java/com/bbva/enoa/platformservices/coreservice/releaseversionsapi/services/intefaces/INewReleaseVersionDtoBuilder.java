package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.intefaces;

import com.bbva.enoa.apirestgen.releaseversionsapi.model.NewReleaseVersionDto;
import com.bbva.enoa.apirestgen.releaseversionsapi.model.RVRequestDTO;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.release.entities.Release;

/**
 * Builds a NewReleaseVersionDto from a ReleaseVersion:
 * <p>
 * - Gets Product, Subsystems and Release data.
 * <p>
 * Created by xe52580 on 14/02/2017.
 */
public interface INewReleaseVersionDtoBuilder
{
    /**
     * Builds a DTO for creating a new release version of a product.
     *
     * @param release Release the version belongs to.
     * @param product Productof the release.
     * @param ivUser  the user requester
     * @return NewReleaseVersionDto
     */
    NewReleaseVersionDto build(Release release, Product product, String ivUser);

    /**
     * Builds a DTO for create a new release version
     *
     * @param product Productof the release.
     * @param release Release the version belongs to.
     * @return RVRequestDTO object
     */
    RVRequestDTO build(Product product, Release release);
}
