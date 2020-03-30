package de.samply.share.broker.statistics;

import java.util.List;

public class StatisticFieldDto {
    private String mdrField;
    private List<StatisticValueDto> statisticValueDtos;

    public String getMdrField() {
        return mdrField;
    }

    public void setMdrField(String urn) {
        this.mdrField = urn;
    }

    public List<StatisticValueDto> getStatisticValueDtos() {
        return statisticValueDtos;
    }

    public void setStatisticValueDtos(List<StatisticValueDto> statisticValueDtos) {
        this.statisticValueDtos = statisticValueDtos;
    }
}
