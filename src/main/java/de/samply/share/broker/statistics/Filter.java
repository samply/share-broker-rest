package de.samply.share.broker.statistics;


import java.sql.Timestamp;
import java.util.Date;

public class Filter {

    private Timestamp startDate;
    private Timestamp endDate;

    Timestamp getEndDate() {
        if(endDate == null){
            return new Timestamp(System.currentTimeMillis());
        }
        return endDate;
    }

    public void setEndDate(Timestamp endDate) {
        this.endDate = endDate;
    }

    Timestamp getStartDate() {
        if(startDate == null){
            return Timestamp.valueOf("2000-01-01 10:10:10.0");
        }
        return startDate;
    }

    public void setStartDate(Timestamp startDate) {
        this.startDate = startDate;
    }
}
