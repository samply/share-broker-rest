package de.samply.share.broker.utils.cql;

import de.samply.share.query.field.AbstractQueryFieldDto;
import de.samply.share.query.value.AbstractQueryValueDto;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

class CqlAtomicExpressionBuilder {

    private final CqlExpressionFactory cqlExpressionFactory;

    CqlAtomicExpressionBuilder(CqlExpressionFactory cqlExpressionFactory) {
        this.cqlExpressionFactory = cqlExpressionFactory;
    }

    String createAtomicExpressionStatement(String mdrUrn, String entityType, AbstractQueryFieldDto<?, ?> fieldDto) {
        List<String> atomicExpressions = new ArrayList<>();
        for (AbstractQueryValueDto<?> valueDto : fieldDto.getValuesDto()) {
            CollectionUtils.addIgnoreNull(atomicExpressions, createSingleAtomicExpression(mdrUrn, entityType, valueDto));
        }

        if (atomicExpressions.isEmpty()) {
            return null;
        } else if (atomicExpressions.size() == 1) {
            return atomicExpressions.get(0);
        } else {
            return "(" +
                    StringUtils.join(atomicExpressions, " or ") +
                    ")";
        }
    }

    private String createSingleAtomicExpression(String mdrUrn, String entityType, AbstractQueryValueDto<?> valueDto) {
        CqlExpressionFactory.AtomicExpressionParameter atomicExpressionParameter = cqlExpressionFactory.createAtomicExpressionParameter(mdrUrn, entityType, valueDto);
        return cqlExpressionFactory.getAtomicExpression(atomicExpressionParameter);
    }
}
