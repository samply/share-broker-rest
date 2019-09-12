package de.samply.share.broker.utils.cql;

import de.samply.share.query.entity.SimpleQueryDto;

public class SimpleQueryDto2CqlTransformer {

    private final CqlExpressionFactory cqlExpressionFactory;

    public SimpleQueryDto2CqlTransformer() {
        this.cqlExpressionFactory = new CqlExpressionFactory();
    }

    public String toQuery(SimpleQueryDto queryDto, String entityType) {
        CqlCodesytesmDefinitionsBuilder codesytesmDefinittionsBuilder = new CqlCodesytesmDefinitionsBuilder(cqlExpressionFactory);
        CqlAtomicExpressionBuilder atomicExpressionBuilder = new CqlAtomicExpressionBuilder(cqlExpressionFactory);
        CqlPredicateBuilder predicateBuilder = new CqlPredicateBuilder(cqlExpressionFactory, atomicExpressionBuilder);

        String codesystemDefinitionsStatement = codesytesmDefinittionsBuilder.createCodesystemDefinitionsStatement(queryDto);
        String cqlPredicateStatement = predicateBuilder.createCqlPredicateStatment(queryDto, entityType);

        return cqlExpressionFactory.getPreamble(entityType, codesystemDefinitionsStatement) + cqlPredicateStatement;
    }
}
