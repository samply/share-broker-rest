package de.samply.share.broker.utils.connector;

import java.util.ArrayList;
import java.util.List;

public class IcingaReportItem {

  private String exitStatus;
  private String pluginOutput;
  private List<IcingaPerformanceData> performanceData;

  public IcingaReportItem() {
    performanceData = new ArrayList<>();
  }

  public void setExitStatus(String exitStatus) {
    this.exitStatus = exitStatus;
  }

  public void setPluginOutput(String pluginOutput) {
    this.pluginOutput = pluginOutput;
  }

  public List<IcingaPerformanceData> getPerformanceData() {
    return performanceData;
  }

}
