package de.samply.share.broker.statistics;

import de.samply.share.essentialquery.EssentialSimpleValueDto;

public class StatisticValueDto {
    private String condition;
    private String value;
    private String maxValue;

    public StatisticValueDto(EssentialSimpleValueDto essentialSimpleValueDto) {
        this.condition = essentialSimpleValueDto.getCondition().toString();
        this.value = essentialSimpleValueDto.getValue();
        this.maxValue = essentialSimpleValueDto.getMaxValue();
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getMinValue() {
        return value;
    }

    public void setMinValue(String minValue) {
        this.value = minValue;
    }

    public String getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(String maxValue) {
        this.maxValue = maxValue;
    }
}
