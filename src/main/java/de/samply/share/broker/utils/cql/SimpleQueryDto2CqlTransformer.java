package de.samply.share.broker.utils.cql;

import de.samply.share.query.entity.SimpleQueryDto;
import de.samply.share.query.enums.SimpleValueCondition;
import de.samply.share.query.field.AbstractQueryFieldDto;
import de.samply.share.query.value.AbstractQueryValueDto;
import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.List;

public class SimpleQueryDto2CqlTransformer {

    private CqlExpressionFactory cqlExpressionFactory = new CqlExpressionFactory();

    public String toQuery(SimpleQueryDto queryDto, String entityType) {
        StringBuilder cqlQueryPredicateBuilder = new StringBuilder(MessageFormat.format(cqlExpressionFactory.getPreambleTemplate(), entityType));

        addTermsToAndExpression(cqlQueryPredicateBuilder, entityType, queryDto.getDonorDto().getFieldsDto());
        addTermsToAndExpression(cqlQueryPredicateBuilder, entityType, queryDto.getSampleContextDto().getFieldsDto());
        addTermsToAndExpression(cqlQueryPredicateBuilder, entityType, queryDto.getSampleDto().getFieldsDto());
        addTermsToAndExpression(cqlQueryPredicateBuilder, entityType, queryDto.getEventDto().getFieldsDto());

        return cqlQueryPredicateBuilder.toString();
    }

    private void addTermsToAndExpression(StringBuilder cqlQueryPredicateBuilder, String entityType, List<AbstractQueryFieldDto<?, ?>> fieldsDto) {
        for (AbstractQueryFieldDto<?, ?> fieldDto : fieldsDto) {
            String mdrUrn = fieldDto.getUrn();

            String atomicExpressions = createAtomicExpressionsChainedByOr(mdrUrn, entityType, fieldDto);

            if (!StringUtils.isEmpty(atomicExpressions)) {
                String pathExpression = cqlExpressionFactory.getPathExpression(mdrUrn, entityType, atomicExpressions);
                cqlQueryPredicateBuilder.append(" and (").append(pathExpression).append(")");
            }
        }
    }

    private String createAtomicExpressionsChainedByOr(String mdrUrn, String entityType, AbstractQueryFieldDto<?, ?> fieldDto) {
        StringBuilder atomicExpressionBuilder = new StringBuilder();

        boolean isFirstAtomicExpression = true;
        for (AbstractQueryValueDto<?> valueDto : fieldDto.getValuesDto()) {
            isFirstAtomicExpression = addSingleAtomicExpression(atomicExpressionBuilder, mdrUrn, entityType, isFirstAtomicExpression, valueDto);
        }

        if (isFirstAtomicExpression) {
            // if no values have been added
            atomicExpressionBuilder.append("true");
        }

        atomicExpressionBuilder.append(")");

        return atomicExpressionBuilder.toString();
    }

    private boolean addSingleAtomicExpression(StringBuilder atomicExpressionBuilder, String mdrUrn, String entityType, boolean isFirstAtomicExpression, AbstractQueryValueDto<?> valueDto) {
        String atomicExpression = calculateAtomicExpression(mdrUrn, entityType, valueDto);

        if (StringUtils.isEmpty(atomicExpression)) {
            return isFirstAtomicExpression;
        }

        if (isFirstAtomicExpression) {
            isFirstAtomicExpression = false;
            atomicExpressionBuilder.append("(");
        } else {
            atomicExpressionBuilder.append(" or ");
        }

        atomicExpressionBuilder.append(atomicExpression);

        return isFirstAtomicExpression;
    }

    private String calculateAtomicExpression(String mdrUrn, String entityType, AbstractQueryValueDto<?> valueDto) {
        String operator = getOperatorName(valueDto.getCondition());

        if (valueDto.getCondition() != SimpleValueCondition.BETWEEN) {
            return cqlExpressionFactory.getAtomicExpression(mdrUrn, entityType, operator, valueDto.getValueAsXmlString());
        } else {
            return cqlExpressionFactory.getAtomicExpression(mdrUrn, entityType, operator, valueDto.getValueAsXmlString(), valueDto.getMaxValueAsXmlString());
        }
    }

    private String getOperatorName(SimpleValueCondition condition) {
        switch (condition) {
            case BETWEEN:
                return "...";
            case EQUALS:
                return "=";
            case LIKE:
                return "~";
            case GREATER:
                return ">";
            case LESS:
                return "<";
            case NOT_EQUALS:
                return "<>";
            case LESS_OR_EQUALS:
                return "<=";
            case GREATER_OR_EQUALS:
                return ">=";
            default:
                return "default";
        }
    }
}
