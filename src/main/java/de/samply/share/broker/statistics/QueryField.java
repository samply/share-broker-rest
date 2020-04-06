package de.samply.share.broker.statistics;

import de.samply.share.broker.model.db.tables.pojos.StatisticsField;

import java.util.ArrayList;
import java.util.List;

public class QueryField {
    private int id;
    List<StatisticsField> statisticsFields;

    public QueryField(int id) {
        this.id = id;
        statisticsFields = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<StatisticsField> getStatisticsFields() {
        return statisticsFields;
    }

    public void setStatisticsFields(List<StatisticsField> statisticsFields) {
        this.statisticsFields = statisticsFields;
    }
}
