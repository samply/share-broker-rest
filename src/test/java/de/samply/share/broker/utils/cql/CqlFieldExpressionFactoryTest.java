package de.samply.share.broker.utils.cql;

import de.samply.share.query.field.FieldDecimalDto;
import org.easymock.EasyMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static de.samply.share.broker.utils.cql.CqlTestHelper.ENTITY_TYPE;
import static de.samply.share.broker.utils.cql.CqlTestHelper.MDR_URN_1;
import static org.easymock.EasyMock.replay;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import static org.easymock.EasyMock.*;

class CqlFieldExpressionFactoryTest {

    private CqlExpressionFactory expressionFactory;
    private CqlValuesExpressionFactory valuesExpressionFactory;
    private CqlFieldExpressionFactory fieldExpressionFactory;

    @BeforeEach
    void initFactory() {
        expressionFactory = EasyMock.createNiceMock(CqlExpressionFactory.class);
        valuesExpressionFactory = EasyMock.createNiceMock(CqlValuesExpressionFactory.class);

        fieldExpressionFactory = new CqlFieldExpressionFactory(expressionFactory, valuesExpressionFactory);
    }

    @Test
    void test_create_emptyValueExpression() {
        expect(valuesExpressionFactory.create(anyString(), anyString(), anyObject())).andReturn(null);
        expect(expressionFactory.getPathExpression(anyString(), anyString(), anyString())).andReturn("path-expression");

        replay(expressionFactory, valuesExpressionFactory);

        FieldDecimalDto fieldDto = CqlTestHelper.createFieldDto();

        String atomicExpression = fieldExpressionFactory.create(MDR_URN_1, ENTITY_TYPE, fieldDto);
        assertThat("Error creating field expression with missing values expression.", atomicExpression, nullValue());
    }

    @Test
    void test_create_filledValueExpression() {
        expect(valuesExpressionFactory.create(anyString(), anyString(), anyObject())).andReturn("values-expression");
        expect(expressionFactory.getPathExpression(anyString(), anyString(), anyString())).andReturn("path-expression");

        replay(expressionFactory, valuesExpressionFactory);

        FieldDecimalDto fieldDto = CqlTestHelper.createFieldDto();

        String atomicExpression = fieldExpressionFactory.create(MDR_URN_1, ENTITY_TYPE, fieldDto);
        assertThat("Error creating field expression with filled values expression.", atomicExpression, is("path-expression"));
    }


}
