package de.samply.share.broker.utils.cql;

import de.samply.share.query.entity.SimpleQueryDto;
import de.samply.share.query.field.FieldDecimalDto;
import de.samply.share.query.value.ValueDecimalDto;
import org.easymock.EasyMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.*;
import static de.samply.share.broker.utils.cql.CqlTestHelper.*;
import static org.easymock.EasyMock.*;
import static org.hamcrest.CoreMatchers.is;

class CqlPredicateBuilderTest {

    private CqlExpressionFactory expressionFactory;
    private CqlAtomicExpressionBuilder atomicExpressionBuilder;
    private CqlPredicateBuilder predicateBuilder;

    @BeforeEach
    void initPredicateBuilder() {
        expressionFactory = EasyMock.createNiceMock(CqlExpressionFactory.class);
        atomicExpressionBuilder = EasyMock.createNiceMock(CqlAtomicExpressionBuilder.class);

        predicateBuilder = new CqlPredicateBuilder(expressionFactory, atomicExpressionBuilder);
    }

    @Test
    void test_createCqlPredicateStatement_noFields() {
        replay(expressionFactory, atomicExpressionBuilder);

        assertThat("No fields should result in trivial predicate 'true'", predicateBuilder.createCqlPredicateStatment(new SimpleQueryDto(), ENTITY_TYPE), is("true"));
    }

    @Test
    void test_createCqlPredicateStatement_oneFieldWithoutAtomicExpression() {
        ValueDecimalDto valueDto = CqlTestHelper.createValueDto(1, 2);
        FieldDecimalDto fieldDto = CqlTestHelper.createFieldDto(valueDto);
        SimpleQueryDto queryDto = CqlTestHelper.createSimpleQueryDto(fieldDto);

        expect(atomicExpressionBuilder.createAtomicExpressionStatement(anyString(), anyString(), anyObject())).andReturn(null);

        replay(expressionFactory, atomicExpressionBuilder);

        assertThat("No atomic expression should result in trivial predicate 'true'", predicateBuilder.createCqlPredicateStatment(queryDto, ENTITY_TYPE), is("true"));
    }

    @Test
    void test_createCqlPredicateStatement_oneField() {
        ValueDecimalDto valueDto = CqlTestHelper.createValueDto(1, 2);
        FieldDecimalDto fieldDto = CqlTestHelper.createFieldDto(MDR_URN_1, valueDto);
        SimpleQueryDto queryDto = CqlTestHelper.createSimpleQueryDto(fieldDto);

        expect(atomicExpressionBuilder.createAtomicExpressionStatement(eq(MDR_URN_1), eq(ENTITY_TYPE), anyObject())).andReturn("atomic-expression-1");
        expect(expressionFactory.getPathExpression(MDR_URN_1, ENTITY_TYPE, "atomic-expression-1")).andReturn("path-expression-1");

        replay(expressionFactory, atomicExpressionBuilder);

        assertThat("One field with one atomic expression.", predicateBuilder.createCqlPredicateStatment(queryDto, ENTITY_TYPE), is("path-expression-1"));
    }

    @Test
    void test_createCqlPredicateStatement_twoFields() {
        ValueDecimalDto valueDto1 = CqlTestHelper.createValueDto(1, 2);
        FieldDecimalDto fieldDto1 = CqlTestHelper.createFieldDto(MDR_URN_1, valueDto1);

        ValueDecimalDto valueDto2 = CqlTestHelper.createValueDto(1, 2);
        FieldDecimalDto fieldDto2 = CqlTestHelper.createFieldDto(MDR_URN_2, valueDto2);

        SimpleQueryDto queryDto = CqlTestHelper.createSimpleQueryDto(fieldDto1, fieldDto2);

        expect(atomicExpressionBuilder.createAtomicExpressionStatement(eq(MDR_URN_1), eq(ENTITY_TYPE), anyObject())).andReturn("atomic-expression-1");
        expect(atomicExpressionBuilder.createAtomicExpressionStatement(eq(MDR_URN_2), eq(ENTITY_TYPE), anyObject())).andReturn("atomic-expression-2");
        expect(expressionFactory.getPathExpression(MDR_URN_1, ENTITY_TYPE, "atomic-expression-1")).andReturn("path-expression-1");
        expect(expressionFactory.getPathExpression(MDR_URN_2, ENTITY_TYPE, "atomic-expression-2")).andReturn("path-expression-2");

        replay(expressionFactory, atomicExpressionBuilder);

        assertThat("Two field (in one entity) both with atomic expression.", predicateBuilder.createCqlPredicateStatment(queryDto, ENTITY_TYPE), is("path-expression-1 and path-expression-2"));
    }

    @Test
    void test_createCqlPredicateStatement_twoFieldsInTwoEntites() {
        ValueDecimalDto valueDto1 = CqlTestHelper.createValueDto(1, 2);
        FieldDecimalDto fieldDto1 = CqlTestHelper.createFieldDto(MDR_URN_1, valueDto1);

        ValueDecimalDto valueDto2 = CqlTestHelper.createValueDto(1, 2);
        FieldDecimalDto fieldDto2 = CqlTestHelper.createFieldDto(MDR_URN_2, valueDto2);

        SimpleQueryDto queryDto = CqlTestHelper.createSimpleQueryDtoTwoFieldsInTwoEntities(fieldDto1, fieldDto2);

        expect(atomicExpressionBuilder.createAtomicExpressionStatement(eq(MDR_URN_1), eq(ENTITY_TYPE), anyObject())).andReturn("atomic-expression-1");
        expect(atomicExpressionBuilder.createAtomicExpressionStatement(eq(MDR_URN_2), eq(ENTITY_TYPE), anyObject())).andReturn("atomic-expression-2");
        expect(expressionFactory.getPathExpression(MDR_URN_1, ENTITY_TYPE, "atomic-expression-1")).andReturn("path-expression-1");
        expect(expressionFactory.getPathExpression(MDR_URN_2, ENTITY_TYPE, "atomic-expression-2")).andReturn("path-expression-2");

        replay(expressionFactory, atomicExpressionBuilder);

        assertThat("Two field (in two entites) both with atomic expression.", predicateBuilder.createCqlPredicateStatment(queryDto, ENTITY_TYPE), is("path-expression-1 and path-expression-2"));
    }

    @Test
    void test_createCqlPredicateStatement_twoFieldsOneWithoutAtomicExpression() {
        ValueDecimalDto valueDto1 = CqlTestHelper.createValueDto(1, 2);
        FieldDecimalDto fieldDto1 = CqlTestHelper.createFieldDto(MDR_URN_1, valueDto1);

        ValueDecimalDto valueDto2 = CqlTestHelper.createValueDto(1, 2);
        FieldDecimalDto fieldDto2 = CqlTestHelper.createFieldDto(MDR_URN_2, valueDto2);

        SimpleQueryDto queryDto = CqlTestHelper.createSimpleQueryDto(fieldDto1, fieldDto2);

        expect(atomicExpressionBuilder.createAtomicExpressionStatement(eq(MDR_URN_1), eq(ENTITY_TYPE), anyObject())).andReturn(null);
        expect(atomicExpressionBuilder.createAtomicExpressionStatement(eq(MDR_URN_2), eq(ENTITY_TYPE), anyObject())).andReturn("atomic-expression-2");
        expect(expressionFactory.getPathExpression(MDR_URN_1, ENTITY_TYPE, "atomic-expression-1")).andReturn("path-expression-1");
        expect(expressionFactory.getPathExpression(MDR_URN_2, ENTITY_TYPE, "atomic-expression-2")).andReturn("path-expression-2");

        replay(expressionFactory, atomicExpressionBuilder);

        assertThat("Two field one with and one without atomic expression.", predicateBuilder.createCqlPredicateStatment(queryDto, ENTITY_TYPE), is("path-expression-2"));
    }

    @Test
    void test_createCqlPredicateStatement_twoFieldsBothWithoutAtomicExpression() {
        ValueDecimalDto valueDto1 = CqlTestHelper.createValueDto(1, 2);
        FieldDecimalDto fieldDto1 = CqlTestHelper.createFieldDto(MDR_URN_1, valueDto1);

        ValueDecimalDto valueDto2 = CqlTestHelper.createValueDto(1, 2);
        FieldDecimalDto fieldDto2 = CqlTestHelper.createFieldDto(MDR_URN_2, valueDto2);

        SimpleQueryDto queryDto = CqlTestHelper.createSimpleQueryDto(fieldDto1, fieldDto2);

        expect(atomicExpressionBuilder.createAtomicExpressionStatement(eq(MDR_URN_1), eq(ENTITY_TYPE), anyObject())).andReturn(null);
        expect(atomicExpressionBuilder.createAtomicExpressionStatement(eq(MDR_URN_2), eq(ENTITY_TYPE), anyObject())).andReturn(null);
        expect(expressionFactory.getPathExpression(MDR_URN_1, ENTITY_TYPE, "atomic-expression-1")).andReturn("path-expression-1");
        expect(expressionFactory.getPathExpression(MDR_URN_2, ENTITY_TYPE, "atomic-expression-2")).andReturn("path-expression-2");

        replay(expressionFactory, atomicExpressionBuilder);

        assertThat("No atomic expression in all fields should result in trivial predicate 'true'", predicateBuilder.createCqlPredicateStatment(queryDto, ENTITY_TYPE), is("true"));
    }
}
