package de.samply.share.broker.utils.cql;

import de.samply.share.query.entity.SimpleQueryDto;
import de.samply.share.query.field.AbstractQueryFieldDto;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

class CqlPredicateBuilder {

    private final CqlExpressionFactory cqlExpressionFactory;
    private final CqlAtomicExpressionBuilder atomicExpressionBuilder;

    CqlPredicateBuilder(CqlExpressionFactory cqlExpressionFactory, CqlAtomicExpressionBuilder atomicExpressionBuilder) {
        this.cqlExpressionFactory = cqlExpressionFactory;
        this.atomicExpressionBuilder = atomicExpressionBuilder;
    }

    String createCqlPredicateStatment(SimpleQueryDto queryDto, String entityType) {
        List<String> predicateList = new ArrayList<>();

        CollectionUtils.addIgnoreNull(predicateList, createCqlPredicate(entityType, queryDto.getDonorDto().getFieldsDto()));
        CollectionUtils.addIgnoreNull(predicateList, createCqlPredicate(entityType, queryDto.getSampleContextDto().getFieldsDto()));
        CollectionUtils.addIgnoreNull(predicateList, createCqlPredicate(entityType, queryDto.getSampleDto().getFieldsDto()));
        CollectionUtils.addIgnoreNull(predicateList, createCqlPredicate(entityType, queryDto.getEventDto().getFieldsDto()));

        if (predicateList.isEmpty()) {
            return "true";
        } else {
            return StringUtils.join(predicateList, " and ");
        }
    }

    private String createCqlPredicate(String entityType, List<AbstractQueryFieldDto<?, ?>> fieldsDto) {
        List<String> pathExpressionList = new ArrayList<>();

        for (AbstractQueryFieldDto<?, ?> fieldDto : fieldsDto) {
            String mdrUrn = fieldDto.getUrn();
            String atomicExpressions = atomicExpressionBuilder.createAtomicExpressionStatement(mdrUrn, entityType, fieldDto);

            if (!StringUtils.isEmpty(atomicExpressions)) {
                String pathExpression = cqlExpressionFactory.getPathExpression(mdrUrn, entityType, atomicExpressions);

                pathExpressionList.add(pathExpression);
            }
        }

        if (pathExpressionList.isEmpty()) {
            return null;
        } else if (pathExpressionList.size() == 1) {
            return pathExpressionList.get(0);
        } else {
            return StringUtils.join(pathExpressionList, " and ");
        }
    }
}
