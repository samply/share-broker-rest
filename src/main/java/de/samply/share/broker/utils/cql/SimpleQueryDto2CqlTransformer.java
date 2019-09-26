package de.samply.share.broker.utils.cql;

import de.samply.share.query.entity.SimpleQueryDto;

public class SimpleQueryDto2CqlTransformer {

    private final CqlExpressionFactory cqlExpressionFactory;

    public SimpleQueryDto2CqlTransformer() {
        this.cqlExpressionFactory = new CqlExpressionFactory();
    }

    public String toQuery(SimpleQueryDto queryDto, String entityType) {
        CqlCodesytemDefinitionsFactory codesytemDefinitionsFactory = new CqlCodesytemDefinitionsFactory(cqlExpressionFactory);
        CqlSingletonStatementsFactory singletonsFactory = new CqlSingletonStatementsFactory(cqlExpressionFactory);
        CqlFieldExpressionFactory fieldExpressionFactory = new CqlFieldExpressionFactory(cqlExpressionFactory);
        CqlPredicateFactory predicateFactory = new CqlPredicateFactory(fieldExpressionFactory);

        String codesystemDefinitions = codesytemDefinitionsFactory.create(queryDto);
        String singletonStatements = singletonsFactory.create(queryDto, entityType);
        String cqlPredicate = predicateFactory.create(queryDto, entityType);

        return cqlExpressionFactory.getPreamble(entityType, codesystemDefinitions + "\n\n" + singletonStatements) + cqlPredicate;
    }
}
