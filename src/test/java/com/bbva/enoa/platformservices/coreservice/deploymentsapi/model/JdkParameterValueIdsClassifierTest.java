package com.bbva.enoa.platformservices.coreservice.deploymentsapi.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

public class JdkParameterValueIdsClassifierTest
{
    private JdkParameterValueIdsClassifier classifier;

    @BeforeEach
    public void init()
    {
        classifier = new JdkParameterValueIdsClassifier();
    }

    @Test
    public void when_ids_are_populated_for_addition_then_are_inserted_in_addition_list()
    {
        classifier.populateForAddition(Arrays.asList(1, 2, 3));

        Assertions.assertTrue(classifier.isForAddition(1));
        Assertions.assertTrue(classifier.isForAddition(2));
        Assertions.assertTrue(classifier.isForAddition(3));
    }

    @Test
    public void when_populated_ids_for_addition_exists_in_already_inserted_list_then_insert_not_already_existent()
    {
        classifier.addAlreadySelected(1);
        classifier.populateForAddition(Arrays.asList(1, 2, 3));

        Assertions.assertFalse(classifier.isForAddition(1));
        Assertions.assertTrue(classifier.isForAddition(2));
        Assertions.assertTrue(classifier.isForAddition(3));
    }

    @Test
    public void when_populated_ids_for_addition_exists_in_removal_list_then_insert_not_existing_in_removal_list()
    {
        classifier.addForRemoval(1);
        classifier.populateForAddition(Arrays.asList(1, 2, 3));

        Assertions.assertFalse(classifier.isForAddition(1));
        Assertions.assertTrue(classifier.isForAddition(2));
        Assertions.assertTrue(classifier.isForAddition(3));
    }

    @Test
    public void when_id_is_populated_for_removal_then_is_inserted_in_removal_list()
    {
        classifier.addForRemoval(1);

        Assertions.assertTrue(classifier.isForRemoval(1));
    }

    @Test
    public void when_id_is_populated_for_already_existing_then_is_inserted_in_already_existing_list()
    {
        classifier.addAlreadySelected(1);

        final Set<Integer> allIds = classifier.getAllIds();
        Assertions.assertEquals(1, allIds.size());
        Assertions.assertTrue(allIds.contains(1));
    }

    @Test
    public void when_get_all_ids_then_return_all_ids()
    {
        classifier.addForRemoval(1);
        classifier.addAlreadySelected(2);
        classifier.populateForAddition(Collections.singletonList(3));

        final Set<Integer> result = classifier.getAllIds();

        Assertions.assertEquals(3, result.size());
        Assertions.assertTrue(result.contains(1));
        Assertions.assertTrue(result.contains(2));
        Assertions.assertTrue(result.contains(3));
    }

    @Test
    public void when_no_insertion_and_no_removal_ids_exist_then_return_dont_exist_modified_parameters()
    {
        Assertions.assertFalse(classifier.existsModifiedParameters());
    }

    @Test
    public void when_only_already_inserted_ids_exist_then_return_dont_exist_modified_parameters()
    {
        classifier.addAlreadySelected(1);

        Assertions.assertFalse(classifier.existsModifiedParameters());
    }

    @Test
    public void when_insertion_ids_exist_then_return_exist_modified_parameters()
    {
        classifier.populateForAddition(Collections.singletonList(1));

        Assertions.assertTrue(classifier.existsModifiedParameters());
    }

    @Test
    public void when_removal_ids_exist_then_return_exist_modified_parameters()
    {
        classifier.addForRemoval(1);

        Assertions.assertTrue(classifier.existsModifiedParameters());
    }

    @Test
    public void when_insertion_ids_list_is_empty_then_id_is_not_for_addition()
    {
        Assertions.assertFalse(classifier.isForAddition(1));
    }

    @Test
    public void when_insertion_ids_list_does_not_contain_id_then_id_is_not_for_addition()
    {
        classifier.populateForAddition(Collections.singletonList(1));

        Assertions.assertFalse(classifier.isForAddition(2));
    }

    @Test
    public void when_insertion_ids_list_does_contain_id_then_id_is_for_addition()
    {
        classifier.populateForAddition(Collections.singletonList(1));

        Assertions.assertTrue(classifier.isForAddition(1));
    }

    @Test
    public void when_removal_ids_list_is_empty_then_id_is_not_for_removal()
    {
        Assertions.assertFalse(classifier.isForRemoval(1));
    }

    @Test
    public void when_removal_ids_list_does_not_contain_id_then_id_is_not_for_removal()
    {
        classifier.addForRemoval(1);

        Assertions.assertFalse(classifier.isForRemoval(2));
    }

    @Test
    public void when_removal_ids_list_does_contain_id_then_id_is_for_removal()
    {
        classifier.addForRemoval(1);

        Assertions.assertTrue(classifier.isForRemoval(1));
    }

}