package de.samply.share.broker.utils.cql;

import de.samply.share.essentialquery.EssentialSimpleFieldDto;
import org.apache.commons.lang3.StringUtils;

class CqlFieldExpressionFactory {

    private final CqlExpressionFactory cqlExpressionFactory;
    private final CqlValuesExpressionFactory valuesExpressionFactory;

    CqlFieldExpressionFactory(CqlExpressionFactory cqlExpressionFactory) {
        this(cqlExpressionFactory, new CqlValuesExpressionFactory(cqlExpressionFactory));
    }

    CqlFieldExpressionFactory(CqlExpressionFactory cqlExpressionFactory, CqlValuesExpressionFactory valuesExpressionFactory) {
        this.cqlExpressionFactory = cqlExpressionFactory;
        this.valuesExpressionFactory = valuesExpressionFactory;
    }

    String create(String mdrUrn, String entityType, EssentialSimpleFieldDto fieldDto) {
        String valuesExpression = valuesExpressionFactory.create(mdrUrn, entityType, fieldDto);

        if (!StringUtils.isEmpty(valuesExpression)) {
            return cqlExpressionFactory.getPathExpression(mdrUrn, entityType, valuesExpression);
        } else {
            return null;
        }
    }
}
