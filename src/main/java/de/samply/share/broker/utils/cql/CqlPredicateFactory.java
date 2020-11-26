package de.samply.share.broker.utils.cql;

import de.samply.share.essentialquery.EssentialSimpleFieldDto;
import de.samply.share.essentialquery.EssentialSimpleQueryDto;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

class CqlPredicateFactory {

  private final CqlFieldExpressionFactory fieldExpressionFactory;

  CqlPredicateFactory(CqlFieldExpressionFactory fieldExpressionFactory) {
    this.fieldExpressionFactory = fieldExpressionFactory;
  }

  String create(EssentialSimpleQueryDto queryDto, String entityType) {
    return createCqlPredicate(entityType, queryDto.getFieldDtos());
  }

  private String createCqlPredicate(String entityType, List<EssentialSimpleFieldDto> fieldsDto) {
    List<String> pathExpressionList = new ArrayList<>();

    for (EssentialSimpleFieldDto fieldDto : fieldsDto) {
      String mdrUrn = fieldDto.getUrn();
      CollectionUtils.addIgnoreNull(pathExpressionList,
          fieldExpressionFactory.create(mdrUrn, entityType, fieldDto));
    }

    if (pathExpressionList.isEmpty()) {
      return "true";
    } else {
      return StringUtils.join(pathExpressionList, " and ");
    }
  }
}
