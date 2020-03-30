package de.samply.share.broker.statistics;

import java.util.List;

public class StatisticQueryDto {

    private int id;
    private String date;
    private List<StatisticFieldDto> statisticFieldDtos;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public List<StatisticFieldDto> getStatisticFieldDtos() {
        return statisticFieldDtos;
    }

    public void setStatisticFieldDtos(List<StatisticFieldDto> statisticFieldDtos) {
        this.statisticFieldDtos = statisticFieldDtos;
    }
}
