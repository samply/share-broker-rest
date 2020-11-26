package de.samply.share.broker.utils.cql;

import static de.samply.share.broker.utils.cql.CqlTestHelper.ENTITY_TYPE;
import static de.samply.share.broker.utils.cql.CqlTestHelper.MDR_URN_1;
import static org.easymock.EasyMock.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import de.samply.share.essentialquery.EssentialSimpleFieldDto;
import de.samply.share.essentialquery.EssentialSimpleValueDto;
import java.util.Collections;
import org.easymock.EasyMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CqlValuesExpressionFactoryTest {

  private CqlExpressionFactory expressionFactory;
  private CqlValuesExpressionFactory valuesExpressionFactory;

  @BeforeEach
  void initFactory() {
    expressionFactory = EasyMock.createNiceMock(CqlExpressionFactory.class);

    valuesExpressionFactory = new CqlValuesExpressionFactory(expressionFactory);
  }

  @Test
  void test_create_noValue() {
    replay(expressionFactory);

    EssentialSimpleFieldDto fieldDto = CqlTestHelper.createFieldDto();

    String atomicExpression = valuesExpressionFactory.create(MDR_URN_1, ENTITY_TYPE, fieldDto);
    assertThat("Error creating atomic expression for no values.", atomicExpression, nullValue());
  }

  @Test
  void test_create_oneValue() {
    expect(expressionFactory
        .createAtomicExpressionParameterList(anyString(), anyString(), anyObject(), anyObject()))
        .andStubReturn(Collections.singletonList(null));

    expect(expressionFactory.getAtomicExpression(anyObject())).andReturn("atomic-expression-1");
    replay(expressionFactory);

    EssentialSimpleValueDto valueDto = CqlTestHelper.createValueDto(1, 2);
    EssentialSimpleFieldDto fieldDto = CqlTestHelper.createFieldDto(valueDto);

    String atomicExpression = valuesExpressionFactory.create(MDR_URN_1, ENTITY_TYPE, fieldDto);
    assertThat("Error creating atomic expression for one value.", atomicExpression,
        is("atomic-expression-1"));
  }

  @Test
  void test_create_twoValues() {
    expect(expressionFactory
        .createAtomicExpressionParameterList(anyString(), anyString(), anyObject(), anyObject()))
        .andStubReturn(Collections.singletonList(null));

    expect(expressionFactory.getAtomicExpression(anyObject())).andReturn("atomic-expression-1");
    expect(expressionFactory.getAtomicExpression(anyObject())).andReturn("atomic-expression-2");
    replay(expressionFactory);

    EssentialSimpleValueDto valueDto1 = CqlTestHelper.createValueDto(1, 2);
    EssentialSimpleValueDto valueDto2 = CqlTestHelper.createValueDto(1, 3);
    EssentialSimpleFieldDto fieldDto = CqlTestHelper.createFieldDto(valueDto1, valueDto2);

    String atomicExpression = valuesExpressionFactory.create(MDR_URN_1, ENTITY_TYPE, fieldDto);
    assertThat("Error creating atomic expression for two values.", atomicExpression,
        is("(atomic-expression-1 or atomic-expression-2)"));
  }

  @Test
  void test_create_threeValues() {
    expect(expressionFactory
        .createAtomicExpressionParameterList(anyString(), anyString(), anyObject(), anyObject()))
        .andStubReturn(Collections.singletonList(null));

    expect(expressionFactory.getAtomicExpression(anyObject())).andReturn("atomic-expression-1");
    expect(expressionFactory.getAtomicExpression(anyObject())).andReturn("atomic-expression-2");
    expect(expressionFactory.getAtomicExpression(anyObject())).andReturn("atomic-expression-3");
    replay(expressionFactory);

    EssentialSimpleValueDto valueDto1 = CqlTestHelper.createValueDto(1, 2);
    EssentialSimpleValueDto valueDto2 = CqlTestHelper.createValueDto(1, 3);
    EssentialSimpleValueDto valueDto3 = CqlTestHelper.createValueDto(1, 4);
    EssentialSimpleFieldDto fieldDto = CqlTestHelper
        .createFieldDto(valueDto1, valueDto2, valueDto3);

    String atomicExpression = valuesExpressionFactory.create(MDR_URN_1, ENTITY_TYPE, fieldDto);
    assertThat("Error creating atomic expression for three values.", atomicExpression,
        is("(atomic-expression-1 or atomic-expression-2 or atomic-expression-3)"));
  }

  @Test
  void test_create_threeValuesButOneMissingAtomicExpression() {
    expect(expressionFactory
        .createAtomicExpressionParameterList(anyString(), anyString(), anyObject(), anyObject()))
        .andStubReturn(Collections.singletonList(null));

    expect(expressionFactory.getAtomicExpression(anyObject())).andReturn("atomic-expression-1");
    expect(expressionFactory.getAtomicExpression(anyObject())).andReturn(null);
    expect(expressionFactory.getAtomicExpression(anyObject())).andReturn("atomic-expression-3");
    replay(expressionFactory);

    EssentialSimpleValueDto valueDto1 = CqlTestHelper.createValueDto(1, 2);
    EssentialSimpleValueDto valueDto2 = CqlTestHelper.createValueDto(1, 3);
    EssentialSimpleValueDto valueDto3 = CqlTestHelper.createValueDto(1, 4);
    EssentialSimpleFieldDto fieldDto = CqlTestHelper
        .createFieldDto(valueDto1, valueDto2, valueDto3);

    String atomicExpression = valuesExpressionFactory.create(MDR_URN_1, ENTITY_TYPE, fieldDto);
    assertThat(
        "Error creating atomic expression for three values but with one missing atomic expression.",
        atomicExpression, is("(atomic-expression-1 or atomic-expression-3)"));
  }

}
