package de.samply.share.broker.utils.cql;

import de.samply.share.query.entity.SimpleQueryDto;
import de.samply.share.query.enums.SimpleValueCondition;
import de.samply.share.query.field.AbstractQueryFieldDto;
import de.samply.share.query.value.AbstractQueryValueDto;
import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SimpleQueryDto2CqlTransformer {

    private final CqlExpressionFactory cqlExpressionFactory;

    public SimpleQueryDto2CqlTransformer() {
        this(new CqlExpressionFactory());
    }

    SimpleQueryDto2CqlTransformer(CqlExpressionFactory cqlExpressionFactory) {
        this.cqlExpressionFactory = cqlExpressionFactory;
    }

    public String toQuery(SimpleQueryDto queryDto, String entityType) {
        StringBuilder cqlQueryPredicateBuilder = new StringBuilder();
        Set cqlLibraries = new HashSet();

        addTermsToAndExpression(cqlQueryPredicateBuilder, cqlLibraries, entityType, queryDto.getDonorDto().getFieldsDto());
        cqlQueryPredicateBuilder.append(" and ");
        addTermsToAndExpression(cqlQueryPredicateBuilder, cqlLibraries, entityType, queryDto.getSampleContextDto().getFieldsDto());
        cqlQueryPredicateBuilder.append(" and ");
        addTermsToAndExpression(cqlQueryPredicateBuilder, cqlLibraries, entityType, queryDto.getSampleDto().getFieldsDto());
        cqlQueryPredicateBuilder.append(" and ");
        addTermsToAndExpression(cqlQueryPredicateBuilder, cqlLibraries, entityType, queryDto.getEventDto().getFieldsDto());

        return cqlExpressionFactory.getPreamble(entityType, StringUtils.join(cqlLibraries, "\n")) + cqlQueryPredicateBuilder.toString();
    }

    private void addTermsToAndExpression(StringBuilder cqlQueryPredicateBuilder, Set cqlLibraries, String entityType, List<AbstractQueryFieldDto<?, ?>> fieldsDto) {
        boolean isFirstField = true;

        for (AbstractQueryFieldDto<?, ?> fieldDto : fieldsDto) {
            String mdrUrn = fieldDto.getUrn();

            String codesystemName = cqlExpressionFactory.getCodesystemName(mdrUrn);
            String codesystemUrl = cqlExpressionFactory.getCodesystemUrl(mdrUrn);
            if (!StringUtils.isBlank(codesystemName) && !StringUtils.isBlank(codesystemUrl)) {
                cqlLibraries.add(MessageFormat.format("codesystem {0}: ''{1}''", codesystemName, codesystemUrl));
            }

            String atomicExpressions = createAtomicExpressionsChainedByOr(mdrUrn, entityType, fieldDto);

            if (!StringUtils.isEmpty(atomicExpressions)) {
                String pathExpression = cqlExpressionFactory.getPathExpression(mdrUrn, entityType, atomicExpressions);
                if (isFirstField) {
                    isFirstField = false;
                } else {
                    cqlQueryPredicateBuilder.append(" and ");
                }

                cqlQueryPredicateBuilder.append(pathExpression);
            }
        }

        // if expression would be empty
        if (isFirstField) {
            cqlQueryPredicateBuilder.append("true");
        }
    }

    private String createAtomicExpressionsChainedByOr(String mdrUrn, String entityType, AbstractQueryFieldDto<?, ?> fieldDto) {
        StringBuilder atomicExpressionBuilder = new StringBuilder();

        boolean isFirstAtomicExpression = true;
        for (AbstractQueryValueDto<?> valueDto : fieldDto.getValuesDto()) {
            isFirstAtomicExpression = addSingleAtomicExpression(atomicExpressionBuilder, mdrUrn, entityType, isFirstAtomicExpression, valueDto);
        }

        // if atomic expression would be empty
        if (isFirstAtomicExpression) {
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
