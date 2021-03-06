package de.samply.share.broker.statistics;

import de.samply.share.broker.jdbc.ResourceManager;
import de.samply.share.broker.model.db.Tables;
import de.samply.share.broker.model.db.enums.EssentialValueType;
import de.samply.share.broker.model.db.enums.SimpleValueCondition;
import de.samply.share.broker.model.db.tables.pojos.StatisticsField;
import de.samply.share.broker.model.db.tables.pojos.StatisticsQuery;
import de.samply.share.broker.model.db.tables.pojos.StatisticsValue;
import de.samply.share.essentialquery.EssentialSimpleFieldDto;
import de.samply.share.essentialquery.EssentialSimpleQueryDto;
import de.samply.share.essentialquery.EssentialSimpleValueDto;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import org.jooq.DSLContext;

public class StatisticsHandler {

  /**
   * Todo.
   * @param queryDto Todo.
   * @param inquiryId Todo.
   */
  public void save(EssentialSimpleQueryDto queryDto, Integer inquiryId) {
    int id = saveQuery(inquiryId);

    for (EssentialSimpleFieldDto fieldDto : queryDto.getFieldDtos()) {
      int fieldId = saveField(fieldDto, id);

      for (EssentialSimpleValueDto valueDto : fieldDto.getValueDtos()) {
        saveValue(valueDto, fieldId);
      }
    }
  }

  private int saveValue(EssentialSimpleValueDto valueDto, int fieldId) {
    try (Connection connection = ResourceManager.getConnection()) {
      StatisticsValue statisticsValue = new StatisticsValue();
      statisticsValue.setFieldid(fieldId);
      statisticsValue.setValue(valueDto.getValue());
      statisticsValue.setMaxvalue(valueDto.getMaxValue());
      statisticsValue.setCondition(translate(valueDto.getCondition()));

      DSLContext dslContext = ResourceManager.getDslContext(connection);

      dslContext
          .insertInto(Tables.STATISTICS_VALUE,
              Tables.STATISTICS_VALUE.FIELDID,
              Tables.STATISTICS_VALUE.VALUE,
              Tables.STATISTICS_VALUE.MAXVALUE,
              Tables.STATISTICS_VALUE.CONDITION)
          .values(statisticsValue.getFieldid(),
              statisticsValue.getValue(),
              statisticsValue.getMaxvalue(),
              statisticsValue.getCondition())
          .execute();

      return dslContext.lastID().intValue();
    } catch (SQLException e) {
      e.printStackTrace();
      return -1;
    }
  }

  private SimpleValueCondition translate(
      de.samply.share.query.enums.SimpleValueCondition condition) {
    switch (condition) {
      case EQUALS:
        return SimpleValueCondition.STATCOND_EQUALS;
      case NOT_EQUALS:
        return SimpleValueCondition.STATCOND_NOT_EQUALS;
      case LESS:
        return SimpleValueCondition.STATCOND_LESS;
      case LESS_OR_EQUALS:
        return SimpleValueCondition.STATCOND_LESS_OR_EQUALS;
      case GREATER:
        return SimpleValueCondition.STATCOND_GREATER;
      case GREATER_OR_EQUALS:
        return SimpleValueCondition.STATCOND_GREATER_OR_EQUALS;
      case BETWEEN:
        return SimpleValueCondition.STATCOND_BETWEEN;

      default:
        return null;
    }
  }

  private EssentialValueType translate(
      de.samply.share.essentialquery.EssentialValueType valueType) {
    switch (valueType) {
      case STRING:
        return EssentialValueType.STATTYPE_STRING;
      case INTEGER:
        return EssentialValueType.STATTYPE_INTEGER;
      case DECIMAL:
        return EssentialValueType.STATTYPE_DECIMAL;
      case DATE:
        return EssentialValueType.STATTYPE_DATE;
      case DATETIME:
        return EssentialValueType.STATTYPE_DATETIME;
      case PERMITTEDVALUE:
        return EssentialValueType.STATTYPE_PERMITTEDVALUE;

      default:
        return null;
    }
  }


  private int saveField(EssentialSimpleFieldDto fieldDto, int id) {
    try (Connection connection = ResourceManager.getConnection()) {
      StatisticsField statisticsField = new StatisticsField();
      statisticsField.setQueryid(id);
      statisticsField.setUrn(fieldDto.getUrn());
      statisticsField.setValuetype(translate(fieldDto.getValueType()));

      DSLContext dslContext = ResourceManager.getDslContext(connection);

      dslContext
          .insertInto(Tables.STATISTICS_FIELD,
              Tables.STATISTICS_FIELD.QUERYID,
              Tables.STATISTICS_FIELD.URN,
              Tables.STATISTICS_FIELD.VALUETYPE)
          .values(statisticsField.getQueryid(),
              statisticsField.getUrn(),
              statisticsField.getValuetype())
          .execute();

      return dslContext.lastID().intValue();
    } catch (SQLException e) {
      e.printStackTrace();
      return -1;
    }
  }

  private int saveQuery(Integer inquiryId) {
    try (Connection connection = ResourceManager.getConnection()) {
      StatisticsQuery statisticsQuery = new StatisticsQuery();
      statisticsQuery.setInquiryid(inquiryId);
      statisticsQuery.setCreated(Timestamp.valueOf(LocalDateTime.now()));

      DSLContext dslContext = ResourceManager.getDslContext(connection);

      dslContext
          .insertInto(Tables.STATISTICS_QUERY,
              Tables.STATISTICS_QUERY.INQUIRYID,
              Tables.STATISTICS_QUERY.CREATED)
          .values(statisticsQuery.getInquiryid(),
              statisticsQuery.getCreated())
          .execute();

      return dslContext.lastID().intValue();
    } catch (SQLException e) {
      e.printStackTrace();
      return -1;
    }
  }
}
