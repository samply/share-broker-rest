package de.samply.share.broker.utils.connector;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;

/**
 * TypeAdapter for Gson.
 * Makes use of the toString method for a performance data entry instead of converting it to a json
 * string and uses the parser from IcingaPerformanceData to parse a performance data report.
 */
public class PerformanceDataAdapter extends TypeAdapter<IcingaPerformanceData> {

  /**
   * Todo.
   * @param reader Todo.
   * @return Todo.
   * @throws IOException Todo.
   */
  public IcingaPerformanceData read(JsonReader reader) throws IOException {
    if (reader.peek() == JsonToken.NULL) {
      reader.nextNull();
      return null;
    }
    return IcingaPerformanceData.parse(reader.nextString());
  }

  /**
   * Todo.
   * @param writer Todo.
   * @param value Todo.
   * @throws IOException Todo.
   */
  public void write(JsonWriter writer, IcingaPerformanceData value) throws IOException {
    if (value == null) {
      writer.nullValue();
      return;
    }
    writer.value(value.toString());
  }
}
