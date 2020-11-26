package de.samply.share.broker.utils.connector;

import com.google.gson.annotations.JsonAdapter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Performance Data Class.
 * Represents the performance data that is recognized by icinga 2.
 */
@JsonAdapter(PerformanceDataAdapter.class)
public class IcingaPerformanceData {

  public static final char ASSIGNMENT_CHARACTER = '=';
  public static final char VALUE_SEPARATOR = ';';
  public static final char SINGLE_QUOTE = '\'';
  final String name;
  final String value;
  final UnitOfMeasure unitOfMeasure;
  String warnThreshold;
  String critThreshold;
  String minThreshold;
  String maxThreshold;

  public IcingaPerformanceData(String name, String value, UnitOfMeasure unitOfMeasure) {
    this(name, value, unitOfMeasure, null, null, null, null);
  }

  /**
   * Todo.
   * @param name Todo.
   * @param value Todo.
   * @param unitOfMeasure Todo.
   * @param warnThreshold Todo.
   * @param critThreshold Todo.
   * @param minThreshold Todo.
   * @param maxThreshold Todo.
   */
  public IcingaPerformanceData(String name,
      String value,
      UnitOfMeasure unitOfMeasure,
      String warnThreshold,
      String critThreshold,
      String minThreshold,
      String maxThreshold) {
    this.name = name;
    this.value = value;
    this.unitOfMeasure = unitOfMeasure;
    this.warnThreshold = warnThreshold;
    this.critThreshold = critThreshold;
    this.minThreshold = minThreshold;
    this.maxThreshold = maxThreshold;
  }

  /**
   * Try to parse a String to a performance data object.
   *
   * @param in the string to parse
   * @return the performance data object
   */
  public static IcingaPerformanceData parse(String in) {
    try {
      IcingaPerformanceData icingaPerformanceData;
      String[] parts = in.split(";", -1);
      String mandatoryPart = parts[0];

      String[] mandatoryParts = mandatoryPart.split(String.valueOf(ASSIGNMENT_CHARACTER));
      String name = mandatoryParts[0].substring(1, mandatoryParts[0].length() - 1);
      String value = mandatoryParts[1];

      Matcher matcher = Pattern.compile("\\D").matcher(value); // \D = non-digit
      matcher.find();
      int uomIndex = matcher.start();
      if (uomIndex > 0) { // If the last unit is not a decimal, it contains an UOM
        icingaPerformanceData = new IcingaPerformanceData(name, value.substring(0, uomIndex),
            UnitOfMeasure.findByValue(value.substring(uomIndex)));
      } else {
        icingaPerformanceData = new IcingaPerformanceData(name, value, UnitOfMeasure.NONE);
      }

      if (parts.length > 1) {
        icingaPerformanceData.setWarnThreshold(parts[1]);
        icingaPerformanceData.setCritThreshold(parts[2]);
        icingaPerformanceData.setMinThreshold(parts[3]);
        icingaPerformanceData.setMaxThreshold(parts[4]);
      }
      return icingaPerformanceData;
    } catch (NullPointerException | IndexOutOfBoundsException e) {
      return null;
    }
  }

  public void setWarnThreshold(String warnThreshold) {
    this.warnThreshold = warnThreshold;
  }

  public void setCritThreshold(String critThreshold) {
    this.critThreshold = critThreshold;
  }

  public void setMinThreshold(String minThreshold) {
    this.minThreshold = minThreshold;
  }

  public void setMaxThreshold(String maxThreshold) {
    this.maxThreshold = maxThreshold;
  }

  @Override
  public String toString() {
    // 'Bezeichnung'=Wert[UOM];[warn];[crit];[min];[max]
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append(SINGLE_QUOTE);
    stringBuilder.append(name);
    stringBuilder.append(SINGLE_QUOTE);
    stringBuilder.append(ASSIGNMENT_CHARACTER);
    stringBuilder.append(value);
    stringBuilder.append(unitOfMeasure);

    if (critThreshold != null || warnThreshold != null || minThreshold != null
        || maxThreshold != null) {
      stringBuilder.append(VALUE_SEPARATOR);
      if (warnThreshold != null) {
        stringBuilder.append(warnThreshold);
      }
      stringBuilder.append(VALUE_SEPARATOR);
      if (critThreshold != null) {
        stringBuilder.append(critThreshold);
      }
      stringBuilder.append(VALUE_SEPARATOR);
      if (minThreshold != null) {
        stringBuilder.append(minThreshold);
      }
      stringBuilder.append(VALUE_SEPARATOR);
      if (maxThreshold != null) {
        stringBuilder.append(maxThreshold);
      }
    }

    return stringBuilder.toString();
  }

  /**
   * The allowed units of measure for the performance data item.
   */
  public enum UnitOfMeasure {
    NONE(""),
    SECONDS("s"),
    MILISECONDS("ms"),
    MICROSECONDS("us"),
    PERCENT("%"),
    BYTES("B"),
    KILOBYTES("B"),
    MEGABYTES("MB"),
    GIGABYTES("GB"),
    TERABYTES("TB"),
    COUNTER("c");

    private String symbol;

    UnitOfMeasure(String symbol) {
      this.symbol = symbol;
    }

    /**
     * Todo.
     * @param value Todo.
     * @return Todo.
     */
    public static UnitOfMeasure findByValue(String value) {
      for (UnitOfMeasure uom : values()) {
        if (uom.symbol.equals(value)) {
          return uom;
        }
      }
      return NONE;
    }

    @Override
    public String toString() {
      return this.symbol;
    }
  }
}
