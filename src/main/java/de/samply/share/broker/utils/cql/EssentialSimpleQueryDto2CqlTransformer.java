package de.samply.share.broker.utils.cql;

import de.samply.share.essentialquery.EssentialSimpleQueryDto;

public class EssentialSimpleQueryDto2CqlTransformer {

    private final CqlExpressionFactory cqlExpressionFactory;

    public EssentialSimpleQueryDto2CqlTransformer() {
        this.cqlExpressionFactory = new CqlExpressionFactory();
    }

    public String toQuery(EssentialSimpleQueryDto essentialSimpleQueryDto, String entityType) {
        CqlCodesytemDefinitionsFactory codesytemDefinitionsFactory = new CqlCodesytemDefinitionsFactory(cqlExpressionFactory);
        CqlSingletonStatementsFactory singletonsFactory = new CqlSingletonStatementsFactory(cqlExpressionFactory);
        CqlFieldExpressionFactory fieldExpressionFactory = new CqlFieldExpressionFactory(cqlExpressionFactory);
        CqlPredicateFactory predicateFactory = new CqlPredicateFactory(fieldExpressionFactory);

        String codesystemDefinitions = codesytemDefinitionsFactory.create(essentialSimpleQueryDto);
        String singletonStatements = singletonsFactory.create(essentialSimpleQueryDto, entityType);
        String cqlPredicate = predicateFactory.create(essentialSimpleQueryDto, entityType);

        return cqlExpressionFactory.getPreamble(entityType, codesystemDefinitions, singletonStatements) + cqlPredicate;
    }
}
