package com.bbva.enoa.platformservices.coreservice.common.repositories;

import com.bbva.enoa.datamodel.model.todotask.entities.ToDoTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * To do task repository.
 */
public interface ToDoTaskRepository extends JpaRepository <ToDoTask, Integer >
{
    /**
     * Get a list of tasks for the given product id
     * @param productId product id
     * @return list of tasks
     */
    List<ToDoTask> findByProductId(@Param("productId") Integer productId);

}