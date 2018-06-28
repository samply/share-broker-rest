package de.samply.share.broker.rest.utils.connector;

import de.samply.share.broker.utils.connector.IcingaPerformanceData;

import java.util.ArrayList;
import java.util.List;

public class IcingaReportItem {
    private String exit_status;
    private String plugin_output;
    private List<IcingaPerformanceData> performance_data;

    public IcingaReportItem() {
        performance_data = new ArrayList<>();
    }

    public String getExit_status() {
        return exit_status;
    }

    public void setExit_status(String exit_status) {
        this.exit_status = exit_status;
    }

    public String getPlugin_output() {
        return plugin_output;
    }

    public void setPlugin_output(String plugin_output) {
        this.plugin_output = plugin_output;
    }

    public List<IcingaPerformanceData> getPerformance_data() {
        return performance_data;
    }

    public void setPerformance_data(List<IcingaPerformanceData> performance_data) {
        this.performance_data = performance_data;
    }
}
