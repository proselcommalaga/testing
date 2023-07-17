package com.bbva.enoa.platformservices.coreservice.common.repositories.ymlvalidations;

import com.bbva.enoa.datamodel.model.api.entities.AsyncBackToBackApiVersion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AsyncBackToBackApiVersionRepository extends JpaRepository<AsyncBackToBackApiVersion, Integer>
{
	AsyncBackToBackApiVersion findByDefinitionFileHash(final String definitionFileHash);
}