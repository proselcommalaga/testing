package com.bbva.enoa.platformservices.coreservice.common.repositories.ymlvalidations;

import com.bbva.enoa.datamodel.model.api.entities.AsyncBackToFrontApiVersion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AsyncBackToFrontApiVersionRepository extends JpaRepository<AsyncBackToFrontApiVersion, Integer>
{
	AsyncBackToFrontApiVersion findByDefinitionFileHash(final String definitionFileHash);
}
