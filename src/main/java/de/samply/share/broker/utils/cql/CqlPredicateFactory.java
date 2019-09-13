package de.samply.share.broker.utils.cql;

import de.samply.share.query.entity.SimpleQueryDto;
import de.samply.share.query.field.AbstractQueryFieldDto;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

class CqlPredicateFactory {

    private final CqlFieldExpressionFactory fieldExpressionFactory;

    CqlPredicateFactory(CqlFieldExpressionFactory fieldExpressionFactory) {
        this.fieldExpressionFactory = fieldExpressionFactory;
    }

    String create(SimpleQueryDto queryDto, String entityType) {
        return createCqlPredicate(entityType, FieldDtoCollector.collect(queryDto));
    }

    private String createCqlPredicate(String entityType, List<AbstractQueryFieldDto<?, ?>> fieldsDto) {
        List<String> pathExpressionList = new ArrayList<>();

        for (AbstractQueryFieldDto<?, ?> fieldDto : fieldsDto) {
            String mdrUrn = fieldDto.getUrn();
            CollectionUtils.addIgnoreNull(pathExpressionList, fieldExpressionFactory.create(mdrUrn, entityType, fieldDto));
        }

        if (pathExpressionList.isEmpty()) {
            return "true";
        } else {
            return StringUtils.join(pathExpressionList, " and ");
        }
    }

}
