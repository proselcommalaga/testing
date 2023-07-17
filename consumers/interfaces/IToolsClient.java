package com.bbva.enoa.platformservices.coreservice.consumers.interfaces;

import com.bbva.enoa.apirestgen.toolsapi.model.TOProductUserDTO;
import com.bbva.enoa.apirestgen.toolsapi.model.TOSubsystemDTO;
import com.bbva.enoa.apirestgen.toolsapi.model.TOSubsystemsCombinationDTO;

import java.util.List;

public interface IToolsClient
{

    /**
     * Add external tools into a product
     *
     * @param productId Product identifier
     */
    void addExternalToolsToProduct(Integer productId);

    /**
     * Remove external tools from a product
     *
     * @param productId Product identifier
     */
    void removeExternalToolsFromProduct(Integer productId);

    /**
     * Add user into a external tool
     *
     * @param productUserDto Product user dto
     */
    void addUserTool(TOProductUserDTO productUserDto);

    /**
     * Delete all user tools
     *
     * @param productUserDto Product user dto
     * @param forceDeletion  Force Deletion
     */
    void removeUserTool(TOProductUserDTO productUserDto, boolean forceDeletion);

    /**
     * Get subystem by given subsystemId
     *
     * @param subsystemId subsystem identifier
     * @return subsystem dto
     */
    TOSubsystemDTO getSubsystemById(final Integer subsystemId);

    /**
     * Get subystem by given repositoryId
     *
     * @param repositoryId subsystem identifier
     * @return subsystem dto
     */
    TOSubsystemDTO getSubsystemByRepositoryId(final Integer repositoryId);

    /**
     * Get subystem by given name and productId
     *
     * @param name      subsystem name
     * @param productId product identifier
     * @return subsystem dto
     */
    TOSubsystemDTO getSubsystemByProductAndName(final String name, final Integer productId);


    /**
     * Get all subsystem of given product
     *
     * @param productId      product identifier
     * @param isBehaviorType isBehaviorType
     * @return all subsystems
     */
    List<TOSubsystemDTO> getProductSubsystems(final Integer productId, final Boolean isBehaviorType);

    /**
     * Get all Subsystems stored in the platform.
     *
     * @return All Subsystems stored in the platform.
     */
    List<TOSubsystemDTO> getAllSubsystems();

    /**
     * Gets an array of DTOs having subsystems information for statistic history loading.
     *
     * @return an array of DTOs having subsystems information for statistic history loading.
     */
    TOSubsystemsCombinationDTO[] getSubsystemsHistorySnapshot();
}
