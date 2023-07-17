package com.bbva.enoa.platformservices.coreservice.common.view;


import lombok.Getter;
import lombok.Setter;

/**
 * StatisticCustom
 */
@Getter
@Setter
public class StatisticCustom
{
    /**
     * Statistic date
     */
    private String statisticDate;

    /**
     * Statistic sum
     */
    private Double pointValue;


    /**
     * Statistic  category
     */
    private String category;

    public StatisticCustom(String statisticDate, Double pointValue, String category)
    {
        this.statisticDate = statisticDate;
        this.pointValue = pointValue;
        this.category = category;
    }
}
