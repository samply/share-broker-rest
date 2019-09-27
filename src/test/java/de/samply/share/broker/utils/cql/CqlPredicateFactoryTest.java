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

class CqlPredicateFactoryTest {

    private CqlFieldExpressionFactory fieldExpressionFactory;
    private CqlPredicateFactory predicateFactory;

    @BeforeEach
    void initPredicateFactory() {
        fieldExpressionFactory = EasyMock.createNiceMock(CqlFieldExpressionFactory.class);

        predicateFactory = new CqlPredicateFactory(fieldExpressionFactory);
    }

    @Test
    void test_create_noFields() {
        replay(fieldExpressionFactory);

        String predicate = predicateFactory.create(new SimpleQueryDto(), ENTITY_TYPE);
        assertThat("No fields should result in trivial predicate 'true'", predicate, is("true"));
    }

    @Test
    void test_create_oneFieldWithoutAtomicExpression() {
        ValueDecimalDto valueDto = CqlTestHelper.createValueDto(1, 2);
        FieldDecimalDto fieldDto = CqlTestHelper.createFieldDto(valueDto);
        SimpleQueryDto queryDto = CqlTestHelper.createSimpleQueryDto(fieldDto);

        expect(fieldExpressionFactory.create(anyString(), anyString(), anyObject())).andReturn(null);

        replay(fieldExpressionFactory);

        String predicate = predicateFactory.create(queryDto, ENTITY_TYPE);
        assertThat("No atomic expression should result in trivial predicate 'true'", predicate, is("true"));
    }

    @Test
    void test_create_oneField() {
        ValueDecimalDto valueDto = CqlTestHelper.createValueDto(1, 2);
        FieldDecimalDto fieldDto = CqlTestHelper.createFieldDto(MDR_URN_1, valueDto);
        SimpleQueryDto queryDto = CqlTestHelper.createSimpleQueryDto(fieldDto);

        expect(fieldExpressionFactory.create(eq(MDR_URN_1), eq(ENTITY_TYPE), anyObject())).andReturn("field-expression-1");

        replay(fieldExpressionFactory);

        String predicate = predicateFactory.create(queryDto, ENTITY_TYPE);
        assertThat("One field with one atomic expression.", predicate, is("field-expression-1"));
    }

    @Test
    void test_create_twoFields() {
        ValueDecimalDto valueDto1 = CqlTestHelper.createValueDto(1, 2);
        FieldDecimalDto fieldDto1 = CqlTestHelper.createFieldDto(MDR_URN_1, valueDto1);

        ValueDecimalDto valueDto2 = CqlTestHelper.createValueDto(1, 2);
        FieldDecimalDto fieldDto2 = CqlTestHelper.createFieldDto(MDR_URN_2, valueDto2);

        SimpleQueryDto queryDto = CqlTestHelper.createSimpleQueryDto(fieldDto1, fieldDto2);

        expect(fieldExpressionFactory.create(eq(MDR_URN_1), eq(ENTITY_TYPE), anyObject())).andReturn("field-expression-1");
        expect(fieldExpressionFactory.create(eq(MDR_URN_2), eq(ENTITY_TYPE), anyObject())).andReturn("field-expression-2");

        replay(fieldExpressionFactory);

        String predicate = predicateFactory.create(queryDto, ENTITY_TYPE);
        assertThat("Two field (in one entity) both with atomic expression.", predicate, is("field-expression-1 and field-expression-2"));
    }

    @Test
    void test_create_twoFieldsInTwoEntites() {
        ValueDecimalDto valueDto1 = CqlTestHelper.createValueDto(1, 2);
        FieldDecimalDto fieldDto1 = CqlTestHelper.createFieldDto(MDR_URN_1, valueDto1);

        ValueDecimalDto valueDto2 = CqlTestHelper.createValueDto(1, 2);
        FieldDecimalDto fieldDto2 = CqlTestHelper.createFieldDto(MDR_URN_2, valueDto2);

        SimpleQueryDto queryDto = CqlTestHelper.createSimpleQueryDtoTwoFieldsInTwoEntities(fieldDto1, fieldDto2);

        expect(fieldExpressionFactory.create(eq(MDR_URN_1), eq(ENTITY_TYPE), anyObject())).andReturn("field-expression-1");
        expect(fieldExpressionFactory.create(eq(MDR_URN_2), eq(ENTITY_TYPE), anyObject())).andReturn("field-expression-2");

        replay(fieldExpressionFactory);

        String predicate = predicateFactory.create(queryDto, ENTITY_TYPE);
        assertThat("Two field (in two entites) both with atomic expression.", predicate, is("field-expression-1 and field-expression-2"));
    }

    @Test
    void test_create_twoFieldsOneWithoutAtomicExpression() {
        ValueDecimalDto valueDto1 = CqlTestHelper.createValueDto(1, 2);
        FieldDecimalDto fieldDto1 = CqlTestHelper.createFieldDto(MDR_URN_1, valueDto1);

        ValueDecimalDto valueDto2 = CqlTestHelper.createValueDto(1, 2);
        FieldDecimalDto fieldDto2 = CqlTestHelper.createFieldDto(MDR_URN_2, valueDto2);

        SimpleQueryDto queryDto = CqlTestHelper.createSimpleQueryDto(fieldDto1, fieldDto2);

        expect(fieldExpressionFactory.create(eq(MDR_URN_1), eq(ENTITY_TYPE), anyObject())).andReturn(null);
        expect(fieldExpressionFactory.create(eq(MDR_URN_2), eq(ENTITY_TYPE), anyObject())).andReturn("field-expression-2");

        replay(fieldExpressionFactory);

        String predicate = predicateFactory.create(queryDto, ENTITY_TYPE);
        assertThat("Two field one with and one without atomic expression.", predicate, is("field-expression-2"));
    }

    @Test
    void test_create_twoFieldsBothWithoutAtomicExpression() {
        ValueDecimalDto valueDto1 = CqlTestHelper.createValueDto(1, 2);
        FieldDecimalDto fieldDto1 = CqlTestHelper.createFieldDto(MDR_URN_1, valueDto1);

        ValueDecimalDto valueDto2 = CqlTestHelper.createValueDto(1, 2);
        FieldDecimalDto fieldDto2 = CqlTestHelper.createFieldDto(MDR_URN_2, valueDto2);

        SimpleQueryDto queryDto = CqlTestHelper.createSimpleQueryDto(fieldDto1, fieldDto2);

        expect(fieldExpressionFactory.create(eq(MDR_URN_1), eq(ENTITY_TYPE), anyObject())).andReturn(null);
        expect(fieldExpressionFactory.create(eq(MDR_URN_2), eq(ENTITY_TYPE), anyObject())).andReturn(null);

        replay(fieldExpressionFactory);

        String predicate = predicateFactory.create(queryDto, ENTITY_TYPE);
        assertThat("No atomic expression in all fields should result in trivial predicate 'true'", predicate, is("true"));
    }
}
