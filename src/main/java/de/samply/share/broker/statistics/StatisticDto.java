package de.samply.share.broker.statistics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StatisticDto {

    private List<StatisticQueryDto> statisticQueryDtos;
    private int queryCount;
    private HashMap<String,Integer> mdrFields;
    private HashMap<String,Integer> mdrFieldsPerQuery;

    StatisticDto(){
        this.statisticQueryDtos = new ArrayList<>();
    }

    public List<StatisticQueryDto> getStatisticQueryDtos() {
        return statisticQueryDtos;
    }

    public void setStatisticQueryDtos(List<StatisticQueryDto> statisticQueryDtos) {
        this.statisticQueryDtos = statisticQueryDtos;
    }

    public int getQueryCount() {
        return queryCount;
    }

    public void setQueryCount(int queryCount) {
        this.queryCount = queryCount;
    }

    public HashMap<String, Integer> getMdrFields() {
        return mdrFields;
    }

    public void setMdrFields(HashMap<String, Integer> mdrFields) {
        this.mdrFields = mdrFields;
    }

    public HashMap<String, Integer> getMdrFieldsPerQuery() {
        return mdrFieldsPerQuery;
    }

    public void setMdrFieldsPerQuery(HashMap<String, Integer> mdrFieldsPerQuery) {
        this.mdrFieldsPerQuery = mdrFieldsPerQuery;
    }
}
