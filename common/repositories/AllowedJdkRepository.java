package com.bbva.enoa.platformservices.coreservice.common.repositories;

import com.bbva.enoa.datamodel.model.release.entities.AllowedJdk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AllowedJdkRepository extends JpaRepository<AllowedJdk, Integer>
{
    /**
     * Return the number of allowed JDK in database bound to a JVM and a JDK.
     *
     * @param jvmVersion The JVM version.
     * @param jdk        The JDK version.
     * @return The number of allowed JDK in database bound to a JVM and a JDK.
     */
    @Query("select count(1) from com.bbva.enoa.datamodel.model.release.entities.AllowedJdk as ajdk " +
            "where ajdk.jvmVersion = :jvmVersion and ajdk.jdk = :jdk")
    Integer countByJvmVersionAndJdk(final String jvmVersion, final String jdk);

    /**
     * Find the allowed JDK in database bound to a JVM and a JDK.
     *
     * @param jvmVersion The JVM version.
     * @param jdk        The JDK version.
     * @return The allowed JDK in database bound to a JVM and a JDK.
     */
    @Query("select ajdk from com.bbva.enoa.datamodel.model.release.entities.AllowedJdk as ajdk " +
            "where ajdk.jvmVersion = :jvmVersion and ajdk.jdk = :jdk")
    AllowedJdk findByJvmVersionAndJdk(final String jvmVersion, final String jdk);
}
