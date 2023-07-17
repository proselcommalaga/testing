package com.bbva.enoa.platformservices.coreservice.common.repositories;

import com.bbva.enoa.datamodel.model.release.entities.AllowedJdkParameterProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface AllowedJdkParameterProductRepository extends JpaRepository<AllowedJdkParameterProduct, Integer>
{
    @Query(value = "select jpt.name as parameterTypeName " +
            ", jpt.description as parameterTypeDescription  " +
            ", jpt.is_exclusive as isExclusive  " +
            ", ajpp.id as jdkVersionParameterId  " +
            ", jp.name as parameterName  " +
            ", jp.description as parameterDescription  " +
            ", ajpp.is_default as parameterDefault  " +
            ", (select aa.allowed_jdk_parameter_product_id " +
            "from deployment_service_allowed_jdk_parameter_value aa " +
            "where aa.deployment_service_id = :deploymentServiceId " +
            "and aa.allowed_jdk_parameter_product_id = ajpp.id) as parameterProductValueId " +
            "from jdk_parameter_type jpt  " +
            "inner join jdk_parameter jp on jpt.id = jp.jdk_parameter_type_id  " +
            "inner join allowed_jdk_parameter_product ajpp on jp.id = ajpp.jdk_parameter_id  " +
            "inner join allowed_jdk aj on aj.id = ajpp.allowed_jdk_id  " +
            "left join product p on p.id = ajpp.product_id    " +
            "where aj.jdk = :jdkName  " +
            "and aj.jvm_version = :jvmVersion  " +
            "and (p.id = cast(cast(:productId as text) as integer) or coalesce(p.id) is null)  " +
            "order by jpt.is_exclusive desc, jpt.name, ajpp.is_default desc, jp.name", nativeQuery = true)
    List<Object[]> getSelectableJdkParameters(@Param("productId") Integer productId, @Param("deploymentServiceId") Integer deploymentServiceId, @Param("jvmVersion") String jvmVersion, @Param("jdkName") String jdkName);

    @Query("select ajpp from com.bbva.enoa.datamodel.model.release.entities.AllowedJdkParameterProduct as ajpp " +
            "where ajpp.id in :ids")
    List<AllowedJdkParameterProduct> findByIdIn(Set<Integer> ids);


}
