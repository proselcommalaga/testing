package com.bbva.enoa.platformservices.coreservice.productsapi.services.interfaces.mapper;

/**
 * @param <T> DTO type
 * @param <D> database type
 */
public interface EntityDtoMapper<T, D>
{
    T fromEntityToDto(D entity);
}
