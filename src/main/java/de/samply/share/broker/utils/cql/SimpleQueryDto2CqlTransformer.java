package de.samply.share.broker.utils.cql;

import de.samply.share.query.entity.SimpleQueryDto;

public class SimpleQueryDto2CqlTransformer {

    private final CqlExpressionFactory cqlExpressionFactory;

    public SimpleQueryDto2CqlTransformer() {
        this.cqlExpressionFactory = new CqlExpressionFactory();
    }

    public String toQuery(SimpleQueryDto queryDto, String entityType) {
        CqlCodesytemDefinitionsFactory codesytemDefinitionsFactory = new CqlCodesytemDefinitionsFactory(cqlExpressionFactory);
        CqlFieldExpressionFactory fieldExpressionFactory = new CqlFieldExpressionFactory(cqlExpressionFactory);
        CqlPredicateFactory predicateFactory = new CqlPredicateFactory(fieldExpressionFactory);

        String codesystemDefinitions = codesytemDefinitionsFactory.create(queryDto);
        String cqlPredicate = predicateFactory.create(queryDto, entityType);

        return cqlExpressionFactory.getPreamble(entityType, codesystemDefinitions) + cqlPredicate;
    }
}
