package de.samply.share.broker.utils.cql;

import de.samply.share.query.field.FieldDecimalDto;
import de.samply.share.query.value.ValueDecimalDto;
import org.easymock.EasyMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.*;
import static de.samply.share.broker.utils.cql.CqlTestHelper.ENTITY_TYPE;
import static de.samply.share.broker.utils.cql.CqlTestHelper.MDR_URN_1;
import static org.easymock.EasyMock.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;

class CqlAtomicExpressionBuilderTest {

    private static final String ATOMIC_EXPRESSION_1 = "atomic-expression-1";
    private static final String ATOMIC_EXPRESSION_2 = "atomic-expression-2";
    private static final String ATOMIC_EXPRESSION_3 = "atomic-expression-3";

    private CqlExpressionFactory expressionFactory;
    private CqlAtomicExpressionBuilder atomicExpressionBuilder;

    @BeforeEach
    void initBuilder() {
        expressionFactory = EasyMock.createNiceMock(CqlExpressionFactory.class);

        atomicExpressionBuilder = new CqlAtomicExpressionBuilder(expressionFactory);
    }

    @Test
    void test_createCodesystemDefinitionsStatement_noValue() {
        replay(expressionFactory);

        FieldDecimalDto fieldDto = CqlTestHelper.createFieldDto();

        String atomicExpression = atomicExpressionBuilder.createAtomicExpressionStatement(MDR_URN_1, ENTITY_TYPE, fieldDto);

        assertThat("Error creating atomic expression for no values.", atomicExpression, nullValue());
    }

    @Test
    void test_createCodesystemDefinitionsStatement_oneValue() {
        expect(expressionFactory.getAtomicExpression(anyObject())).andReturn(ATOMIC_EXPRESSION_1);
        replay(expressionFactory);

        ValueDecimalDto valueDto = CqlTestHelper.createValueDto(1, 2);
        FieldDecimalDto fieldDto = CqlTestHelper.createFieldDto(valueDto);

        String atomicExpression = atomicExpressionBuilder.createAtomicExpressionStatement(MDR_URN_1, ENTITY_TYPE, fieldDto);

        assertThat("Error creating atomic expression for one value.", atomicExpression, is(ATOMIC_EXPRESSION_1));
    }

    @Test
    void test_createCodesystemDefinitionsStatement_twoValues() {
        expect(expressionFactory.getAtomicExpression(anyObject())).andReturn(ATOMIC_EXPRESSION_1);
        expect(expressionFactory.getAtomicExpression(anyObject())).andReturn(ATOMIC_EXPRESSION_2);
        replay(expressionFactory);

        ValueDecimalDto valueDto1 = CqlTestHelper.createValueDto(1, 2);
        ValueDecimalDto valueDto2 = CqlTestHelper.createValueDto(1, 3);
        FieldDecimalDto fieldDto = CqlTestHelper.createFieldDto(valueDto1, valueDto2);

        String atomicExpression = atomicExpressionBuilder.createAtomicExpressionStatement(MDR_URN_1, ENTITY_TYPE, fieldDto);

        assertThat("Error creating atomic expression for two values.", atomicExpression, is("(atomic-expression-1 or atomic-expression-2)"));
    }

    @Test
    void test_createCodesystemDefinitionsStatement_threeValues() {
        expect(expressionFactory.getAtomicExpression(anyObject())).andReturn(ATOMIC_EXPRESSION_1);
        expect(expressionFactory.getAtomicExpression(anyObject())).andReturn(ATOMIC_EXPRESSION_2);
        expect(expressionFactory.getAtomicExpression(anyObject())).andReturn(ATOMIC_EXPRESSION_3);
        replay(expressionFactory);

        ValueDecimalDto valueDto1 = CqlTestHelper.createValueDto(1, 2);
        ValueDecimalDto valueDto2 = CqlTestHelper.createValueDto(1, 3);
        ValueDecimalDto valueDto3 = CqlTestHelper.createValueDto(1, 4);
        FieldDecimalDto fieldDto = CqlTestHelper.createFieldDto(valueDto1, valueDto2, valueDto3);

        String atomicExpression = atomicExpressionBuilder.createAtomicExpressionStatement(MDR_URN_1, ENTITY_TYPE, fieldDto);

        assertThat("Error creating atomic expression for three values.", atomicExpression, is("(atomic-expression-1 or atomic-expression-2 or atomic-expression-3)"));
    }

    @Test
    void test_createCodesystemDefinitionsStatement_threeValuesButOneMissingAtomicExpression() {
        expect(expressionFactory.getAtomicExpression(anyObject())).andReturn(ATOMIC_EXPRESSION_1);
        expect(expressionFactory.getAtomicExpression(anyObject())).andReturn(null);
        expect(expressionFactory.getAtomicExpression(anyObject())).andReturn(ATOMIC_EXPRESSION_3);
        replay(expressionFactory);

        ValueDecimalDto valueDto1 = CqlTestHelper.createValueDto(1, 2);
        ValueDecimalDto valueDto2 = CqlTestHelper.createValueDto(1, 3);
        ValueDecimalDto valueDto3 = CqlTestHelper.createValueDto(1, 4);
        FieldDecimalDto fieldDto = CqlTestHelper.createFieldDto(valueDto1, valueDto2, valueDto3);

        String atomicExpression = atomicExpressionBuilder.createAtomicExpressionStatement(MDR_URN_1, ENTITY_TYPE, fieldDto);

        assertThat("Error creating atomic expression for three values but with one missing atomic expression.", atomicExpression, is("(atomic-expression-1 or atomic-expression-3)"));
    }

}
