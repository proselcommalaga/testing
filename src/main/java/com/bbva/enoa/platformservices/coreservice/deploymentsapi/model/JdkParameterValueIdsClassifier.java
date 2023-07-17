package com.bbva.enoa.platformservices.coreservice.deploymentsapi.model;

import lombok.Getter;
import lombok.ToString;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Class for separating {@link com.bbva.enoa.datamodel.model.release.entities.AllowedJdkParameterProduct} object ids
 * depending on the operation to be made with them. Used specifically in JDK parameters deployment services assignation.
 * When JDK parameters are assigned to a deployment service, there are 3 possibles scenarios:
 * <p>
 * 1. The parameter was already assigned to deployment service. No extra operations.
 * 2. The parameter was assigned but now it's not selected. Remove operation.
 * 3. The parameter was not assigned, but now it's selected. Add operation.
 */
@Getter
@ToString
public class JdkParameterValueIdsClassifier
{
    private Set<Integer> jdkParameterValueIdsToAdd = new HashSet<>();
    private Set<Integer> jdkParameterValueIdsToRemove = new HashSet<>();
    private Set<Integer> jdkParametersAlreadySelected = new HashSet<>();

    public void populateForAddition(List<Integer> selectedParameterIds)
    {
        jdkParameterValueIdsToAdd = selectedParameterIds.stream().filter(id -> !jdkParametersAlreadySelected.contains(id) && !jdkParameterValueIdsToRemove.contains(id)).collect(Collectors.toSet());
    }

    public void addForRemoval(Integer id)
    {
        jdkParameterValueIdsToRemove.add(id);
    }

    public void addAlreadySelected(Integer id)
    {
        jdkParametersAlreadySelected.add(id);
    }

    public Set<Integer> getAllIds()
    {
        Set<Integer> allIds = new HashSet<>(jdkParameterValueIdsToAdd);
        allIds.addAll(jdkParametersAlreadySelected);
        allIds.addAll(jdkParameterValueIdsToRemove);
        return allIds;
    }

    public boolean existsModifiedParameters()
    {
        return !jdkParameterValueIdsToAdd.isEmpty() || !jdkParameterValueIdsToRemove.isEmpty();
    }

    public boolean isForAddition(Integer id)
    {
        return jdkParameterValueIdsToAdd.contains(id);
    }

    public boolean isForRemoval(Integer id)
    {
        return jdkParameterValueIdsToRemove.contains(id);
    }
}
