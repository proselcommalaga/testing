package com.bbva.enoa.platformservices.coreservice.common.repositories;

import com.bbva.enoa.datamodel.model.statistic.entities.Statistic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StatisticRepository extends JpaRepository<Statistic, Long>
{

    /**
     * Get the number of statistics for each day in the given range and for the given filters.
     *
     * @param startDate     Start date, e.g. 2020-01-01.
     * @param endDate       End date, e.g. 2020-12-01.
     * @param statisticType Statistic type
     * @param param1Name    Param name 1
     * @param param1Value   Param value 1
     * @param param2Name    Param name 2
     * @param param2Value   Param value 2
     * @param param3Name    Param name 3
     * @param param3Value   Param value 3
     * @param param4Name    Param name 4
     * @param param4Value   Param value 4
     * @return A map list of date-value pairs.
     */
    @Query(value = "select sa.date, sum(sa.value), sp.param_value " +
            "from statistic sa " +
            "full outer join statistic_param sp on (sa.id = sp.statistic_id and coalesce(:statisticCategory) is not null) " +
            "left join statistic_param t1 on (sa.id = t1.statistic_id and coalesce(:param1Value) is not null) " +
            "left join statistic_param t2 on (sa.id = t2.statistic_id and coalesce(:param2Value) is not null) " +
            "left join statistic_param t3 on (sa.id = t3.statistic_id and coalesce(:param3Value) is not null) " +
            "left join statistic_param t4 on (sa.id = t4.statistic_id and coalesce(:param4Value) is not null) " +
            "left join statistic_param t5 on (sa.id = t5.statistic_id and coalesce(:param5Value) is not null) " +
            "where (coalesce(:param1Value) is null or (t1.param_name = cast(:param1Name as text) and t1.param_value = cast(:param1Value as text))) " +
            "and (coalesce(:param2Value) is null or (t2.param_name = cast(:param2Name as text) and t2.param_value = cast(:param2Value as text))) " +
            "and (coalesce(:param3Value) is null or (t3.param_name = cast(:param3Name as text) and t3.param_value = cast(:param3Value as text))) " +
            "and (coalesce(:param4Value) is null or (t4.param_name = cast(:param4Name as text) and t4.param_value = cast(:param4Value as text))) " +
            "and (coalesce(:param5Value) is null or (t5.param_name = cast(:param5Name as text) and t5.param_value = cast(:param5Value as text))) " +
            "and (coalesce(:statisticCategory) is null or (sp.param_name = cast(:statisticCategory as text))) " +
            "and sa.date >= cast(:startDate as date) and sa.date <= cast(:endDate as date) " +
            "and sa.type = :statisticType " +
            "group by (sa.date, sp.param_value) " +
            "order by (sa.date)",
            nativeQuery = true
    )
    List<Object[]> getTotalValueBetweenDates(
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("statisticType") String statisticType,
            @Param("param1Name") String param1Name,
            @Param("param1Value") String param1Value,
            @Param("param2Name") String param2Name,
            @Param("param2Value") String param2Value,
            @Param("param3Name") String param3Name,
            @Param("param3Value") String param3Value,
            @Param("param4Name") String param4Name,
            @Param("param4Value") String param4Value,
            @Param("param5Name") String param5Name,
            @Param("param5Value") String param5Value,
            @Param("statisticCategory") String statisticCategory);

    /**
     * Get statistics for each day in the given range and for the given filters.
     *
     * @param startDate Start date, e.g. 2020-01-01.
     * @param endDate   End date, e.g. 2020-12-01.
     * @return A map list of date-value pairs.
     */
    @Query(value = "select sa.date, sp.param_value as platform, t1.param_value as environment, t2.param_value as uuaa  " +
            "from statistic sa " +
            "full outer join statistic_param sp on sa.id = sp.statistic_id " +
            "left join statistic_param t1 on sa.id = t1.statistic_id " +
            "left join statistic_param t2 on sa.id = t2.statistic_id " +
            "where coalesce(:environment) is null or t1.param_name = 'CLOUD_PRODUCTS_ENVIRONMENT' and t1.param_value = :environment " +
            "and t1.param_name = 'CLOUD_PRODUCTS_ENVIRONMENT' " +
            "and (coalesce(:uuaa) is null or (t2.param_name = cast('CLOUD_PRODUCTS_UUAA' as text) and t2.param_value = cast(:uuaa as text))) " +
            "and t2.param_name = cast('CLOUD_PRODUCTS_UUAA' as text) " +
            "and sp.param_name = 'CLOUD_PRODUCTS_PLATFORM' " +
            "and sa.date >= cast(:startDate as date) and sa.date <= cast(:endDate as date) " +
            "and sa.type = 'CLOUD_PRODUCTS' " +
            "group by (sa.date, sp.param_value,t2.param_value, t1.param_value) " +
            "order by (sa.date, sp.param_value, t1.param_value,t2.param_value)",
            nativeQuery = true
    )
    List<Object[]> getCloudProductsSummaryExport(
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("environment") String environment,
            @Param("uuaa") String uuaa);

    /**
     * Get the id's of statistics for each day in the given range and for the given filters.
     *
     * @param startDate     Start date, e.g. 2020-01-01.
     * @param endDate       End date, e.g. 2020-12-01.
     * @param statisticType Statistic type
     * @param param1Name    Param name 1
     * @param param1Value   Param value 1
     * @param param2Name    Param name 2
     * @param param2Value   Param value 2
     * @param param3Name    Param name 3
     * @param param3Value   Param value 3
     * @param param4Name    Param name 4
     * @param param4Value   Param value 4
     * @param param5Name    Param name 5
     * @param param5Value   Param value 5
     * @return A map list of id's.
     */
    @Query(value = "select sa.date, coalesce (t1.param_value, 'N/A') param_value1, coalesce (t2.param_value, 'N/A') param_value2, " +
            "coalesce (t3.param_value, 'N/A') param_value3, coalesce (t4.param_value, 'N/A') param_value4, coalesce (t5.param_value, 'N/A') param_value5, sa.value " +
            "from statistic sa " +
            "left join statistic_param t1 on (sa.id = t1.statistic_id and (coalesce(:param1Value) is not null or coalesce(:param1Name) is not null)) " +
            "left join statistic_param t2 on (sa.id = t2.statistic_id and (coalesce(:param2Value) is not null or coalesce(:param2Name) is not null)) " +
            "left join statistic_param t3 on (sa.id = t3.statistic_id and (coalesce(:param3Value) is not null or coalesce(:param3Name) is not null)) " +
            "left join statistic_param t4 on (sa.id = t4.statistic_id and (coalesce(:param4Value) is not null or coalesce(:param4Name) is not null)) " +
            "left join statistic_param t5 on (sa.id = t5.statistic_id and (coalesce(:param5Value) is not null or coalesce(:param5Name) is not null)) " +
            "where (coalesce(:param1Value) is null or (t1.param_value = cast(:param1Value as text))) " +
            "and (coalesce(:param1Name) is null or (t1.param_name = cast(:param1Name as text))) " +
            "and (coalesce(:param2Value) is null or (t2.param_value = cast(:param2Value as text))) " +
            "and (coalesce(:param2Name) is null or (t2.param_name = cast(:param2Name as text))) " +
            "and (coalesce(:param3Value) is null or (t3.param_value = cast(:param3Value as text))) " +
            "and (coalesce(:param3Name) is null or (t3.param_name = cast(:param3Name as text))) " +
            "and (coalesce(:param4Value) is null or (t4.param_value = cast(:param4Value as text))) " +
            "and (coalesce(:param4Name) is null or (t4.param_name = cast(:param4Name as text))) " +
            "and (coalesce(:param5Value) is null or (t5.param_value = cast(:param5Value as text))) " +
            "and (coalesce(:param5Name) is null or (t5.param_name = cast(:param5Name as text))) " +
            "and sa.date >= cast(:startDate as date) and sa.date <= cast(:endDate as date) " +
            "and sa.type = :statisticType " +
            "group by (sa.date, sa.value, t1.param_value, t2.param_value, t3.param_value, t4.param_value, t5.param_value) " +
            "order by (sa.date)",
            nativeQuery = true
    )
    List<Object[]> getStatisticsHistoricalTotalValueBetweenDates(
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("statisticType") String statisticType,
            @Param("param1Name") String param1Name,
            @Param("param1Value") String param1Value,
            @Param("param2Name") String param2Name,
            @Param("param2Value") String param2Value,
            @Param("param3Name") String param3Name,
            @Param("param3Value") String param3Value,
            @Param("param4Name") String param4Name,
            @Param("param4Value") String param4Value,
            @Param("param5Name") String param5Name,
            @Param("param5Value") String param5Value);

}
