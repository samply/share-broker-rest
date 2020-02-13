package de.samply.share.broker.utils.cql;

import de.samply.share.essentialquery.EssentialSimpleFieldDto;
import de.samply.share.essentialquery.EssentialSimpleQueryDto;
import de.samply.share.essentialquery.EssentialSimpleValueDto;
import org.easymock.EasyMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static de.samply.share.broker.utils.cql.CqlTestHelper.*;
import static org.easymock.EasyMock.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

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

        String predicate = predicateFactory.create(new EssentialSimpleQueryDto(), ENTITY_TYPE);
        assertThat("No fields should result in trivial predicate 'true'", predicate, is("true"));
    }

    @Test
    void test_create_oneFieldWithoutAtomicExpression() {
        EssentialSimpleValueDto valueDto = CqlTestHelper.createValueDto(1, 2);
        EssentialSimpleFieldDto fieldDto = CqlTestHelper.createFieldDto(valueDto);
        EssentialSimpleQueryDto queryDto = CqlTestHelper.createSimpleQueryDto(fieldDto);

        expect(fieldExpressionFactory.create(anyString(), anyString(), anyObject())).andReturn(null);

        replay(fieldExpressionFactory);

        String predicate = predicateFactory.create(queryDto, ENTITY_TYPE);
        assertThat("No atomic expression should result in trivial predicate 'true'", predicate, is("true"));
    }

    @Test
    void test_create_oneField() {
        EssentialSimpleValueDto valueDto = CqlTestHelper.createValueDto(1, 2);
        EssentialSimpleFieldDto fieldDto = CqlTestHelper.createFieldDto(MDR_URN_1, valueDto);
        EssentialSimpleQueryDto queryDto = CqlTestHelper.createSimpleQueryDto(fieldDto);

        expect(fieldExpressionFactory.create(eq(MDR_URN_1), eq(ENTITY_TYPE), anyObject())).andReturn("field-expression-1");

        replay(fieldExpressionFactory);

        String predicate = predicateFactory.create(queryDto, ENTITY_TYPE);
        assertThat("One field with one atomic expression.", predicate, is("field-expression-1"));
    }

    @Test
    void test_create_twoFields() {
        EssentialSimpleValueDto valueDto1 = CqlTestHelper.createValueDto(1, 2);
        EssentialSimpleFieldDto fieldDto1 = CqlTestHelper.createFieldDto(MDR_URN_1, valueDto1);

        EssentialSimpleValueDto valueDto2 = CqlTestHelper.createValueDto(1, 2);
        EssentialSimpleFieldDto fieldDto2 = CqlTestHelper.createFieldDto(MDR_URN_2, valueDto2);

        EssentialSimpleQueryDto queryDto = CqlTestHelper.createSimpleQueryDto(fieldDto1, fieldDto2);

        expect(fieldExpressionFactory.create(eq(MDR_URN_1), eq(ENTITY_TYPE), anyObject())).andReturn("field-expression-1");
        expect(fieldExpressionFactory.create(eq(MDR_URN_2), eq(ENTITY_TYPE), anyObject())).andReturn("field-expression-2");

        replay(fieldExpressionFactory);

        String predicate = predicateFactory.create(queryDto, ENTITY_TYPE);
        assertThat("Two field (in one entity) both with atomic expression.", predicate, is("field-expression-1 and field-expression-2"));
    }

    @Test
    void test_create_twoFieldsInTwoEntites() {
        EssentialSimpleValueDto valueDto1 = CqlTestHelper.createValueDto(1, 2);
        EssentialSimpleFieldDto fieldDto1 = CqlTestHelper.createFieldDto(MDR_URN_1, valueDto1);

        EssentialSimpleValueDto valueDto2 = CqlTestHelper.createValueDto(1, 2);
        EssentialSimpleFieldDto fieldDto2 = CqlTestHelper.createFieldDto(MDR_URN_2, valueDto2);

        EssentialSimpleQueryDto queryDto = CqlTestHelper.createSimpleQueryDto(fieldDto1, fieldDto2);

        expect(fieldExpressionFactory.create(eq(MDR_URN_1), eq(ENTITY_TYPE), anyObject())).andReturn("field-expression-1");
        expect(fieldExpressionFactory.create(eq(MDR_URN_2), eq(ENTITY_TYPE), anyObject())).andReturn("field-expression-2");

        replay(fieldExpressionFactory);

        String predicate = predicateFactory.create(queryDto, ENTITY_TYPE);
        assertThat("Two field (in two entites) both with atomic expression.", predicate, is("field-expression-1 and field-expression-2"));
    }

    @Test
    void test_create_twoFieldsOneWithoutAtomicExpression() {
        EssentialSimpleValueDto valueDto1 = CqlTestHelper.createValueDto(1, 2);
        EssentialSimpleFieldDto fieldDto1 = CqlTestHelper.createFieldDto(MDR_URN_1, valueDto1);

        EssentialSimpleValueDto valueDto2 = CqlTestHelper.createValueDto(1, 2);
        EssentialSimpleFieldDto fieldDto2 = CqlTestHelper.createFieldDto(MDR_URN_2, valueDto2);

        EssentialSimpleQueryDto queryDto = CqlTestHelper.createSimpleQueryDto(fieldDto1, fieldDto2);

        expect(fieldExpressionFactory.create(eq(MDR_URN_1), eq(ENTITY_TYPE), anyObject())).andReturn(null);
        expect(fieldExpressionFactory.create(eq(MDR_URN_2), eq(ENTITY_TYPE), anyObject())).andReturn("field-expression-2");

        replay(fieldExpressionFactory);

        String predicate = predicateFactory.create(queryDto, ENTITY_TYPE);
        assertThat("Two field one with and one without atomic expression.", predicate, is("field-expression-2"));
    }

    @Test
    void test_create_twoFieldsBothWithoutAtomicExpression() {
        EssentialSimpleValueDto valueDto1 = CqlTestHelper.createValueDto(1, 2);
        EssentialSimpleFieldDto fieldDto1 = CqlTestHelper.createFieldDto(MDR_URN_1, valueDto1);

        EssentialSimpleValueDto valueDto2 = CqlTestHelper.createValueDto(1, 2);
        EssentialSimpleFieldDto fieldDto2 = CqlTestHelper.createFieldDto(MDR_URN_2, valueDto2);

        EssentialSimpleQueryDto queryDto = CqlTestHelper.createSimpleQueryDto(fieldDto1, fieldDto2);

        expect(fieldExpressionFactory.create(eq(MDR_URN_1), eq(ENTITY_TYPE), anyObject())).andReturn(null);
        expect(fieldExpressionFactory.create(eq(MDR_URN_2), eq(ENTITY_TYPE), anyObject())).andReturn(null);

        replay(fieldExpressionFactory);

        String predicate = predicateFactory.create(queryDto, ENTITY_TYPE);
        assertThat("No atomic expression in all fields should result in trivial predicate 'true'", predicate, is("true"));
    }
}
