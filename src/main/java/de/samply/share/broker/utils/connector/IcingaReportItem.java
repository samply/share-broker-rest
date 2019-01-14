package de.samply.share.broker.utils.connector;

import java.util.ArrayList;
import java.util.List;

public class IcingaReportItem {
    private String exit_status;
    private String plugin_output;
    private List<IcingaPerformanceData> performance_data;

    public IcingaReportItem() {
        performance_data = new ArrayList<>();
    }

    public void setExit_status(String exit_status) {
        this.exit_status = exit_status;
    }

    public void setPlugin_output(String plugin_output) {
        this.plugin_output = plugin_output;
    }

    public List<IcingaPerformanceData> getPerformance_data() {
        return performance_data;
    }

}
