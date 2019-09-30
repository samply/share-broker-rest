package de.samply.share.broker.utils.cql;

import de.samply.share.query.entity.SimpleQueryDto;
import de.samply.share.query.field.FieldDecimalDto;
import de.samply.share.query.value.ValueDecimalDto;
import org.apache.commons.lang.StringUtils;
import org.easymock.EasyMock;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static de.samply.share.broker.utils.cql.CqlTestHelper.*;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class CqlSingletonStatementsFactoryTest {

    private static final String EXPECTED_SINGLETON_STATEMENTS_1 = "define " + SINGLETON_NAME_1 + ": singleton from([" + SINGLETON_NAME_1 + "])";
    private static final String EXPECTED_SINGLETON_STATEMENTS_2 = "define " + SINGLETON_NAME_2 + ": singleton from([" + SINGLETON_NAME_2 + "])";
    
    private CqlExpressionFactory expressionFactory;
    private CqlSingletonStatementsFactory singletonStatementsFactory;

    @BeforeEach
    void initFactory() {
        expressionFactory = EasyMock.createNiceMock(CqlExpressionFactory.class);

        singletonStatementsFactory = new CqlSingletonStatementsFactory(expressionFactory);
    }

    @Test
    void test_create_emptyDto() {
        replay(expressionFactory);

        String singletonStatements = singletonStatementsFactory.create(new SimpleQueryDto(), ENTITY_TYPE);
        assertThat("Error creating singleton statements for empty DTO.", CqlTestHelper.trim(singletonStatements), is(""));
    }

    @Test
    void test_create_WithoutSingleton() {
        SimpleQueryDto queryDto = createDtoWithFields(MDR_URN_1);

        expect(expressionFactory.getSingletons(MDR_URN_1, ENTITY_TYPE)).andReturn(new HashSet<>());

        replay(expressionFactory);

        String singletonStatements = singletonStatementsFactory.create(queryDto, ENTITY_TYPE);
        assertThat("Error creating singleton statements for one field without singleton.", CqlTestHelper.trim(singletonStatements), is(""));
    }

    @Test
    void test_create_WithSingleton() {
        SimpleQueryDto queryDto = createDtoWithFields(MDR_URN_1);

        Set<CqlConfig.Singleton> singletonStatements1 =
                createExpectedSingletonStatementsList(createExpectedSingleton(SINGLETON_NAME_1));
        expect(expressionFactory.getSingletons(MDR_URN_1, ENTITY_TYPE)).andReturn(singletonStatements1);

        replay(expressionFactory);

        String singletonStatements = singletonStatementsFactory.create(queryDto, ENTITY_TYPE);
        assertThat("Error creating singleton statements for one field with singleton.", CqlTestHelper.trim(singletonStatements), is(EXPECTED_SINGLETON_STATEMENTS_1));
    }

    @Test
    void test_create_WithTwoDifferentSingletons() {
        SimpleQueryDto queryDto = createDtoWithFields(MDR_URN_1, MDR_URN_2);

        Set<CqlConfig.Singleton> singletonStatements1 =
                createExpectedSingletonStatementsList(createExpectedSingleton(SINGLETON_NAME_1));
        expect(expressionFactory.getSingletons(MDR_URN_1, ENTITY_TYPE)).andReturn(singletonStatements1);

        Set<CqlConfig.Singleton> singletonStatements2 =
                createExpectedSingletonStatementsList(createExpectedSingleton(SINGLETON_NAME_2));
        expect(expressionFactory.getSingletons(MDR_URN_2, ENTITY_TYPE)).andReturn(singletonStatements2);

        replay(expressionFactory);

        String singletonStatements = singletonStatementsFactory.create(queryDto, ENTITY_TYPE);
        assertThat("Wrong number of singleton statements for Patient.", StringUtils.countMatches(CqlTestHelper.trim(singletonStatements), EXPECTED_SINGLETON_STATEMENTS_1), is(1));
        assertThat("Wrong number of singleton statements for Observation.", StringUtils.countMatches(CqlTestHelper.trim(singletonStatements), EXPECTED_SINGLETON_STATEMENTS_2), is(1));
    }

    @Test
    void test_create_WithTwoDifferentSingletonsInOneField() {
        SimpleQueryDto queryDto = createDtoWithFields(MDR_URN_1);

        Set<CqlConfig.Singleton> singletonStatements1 =
                createExpectedSingletonStatementsList(
                        createExpectedSingleton(SINGLETON_NAME_1),
                        createExpectedSingleton(SINGLETON_NAME_2));
        expect(expressionFactory.getSingletons(MDR_URN_1, ENTITY_TYPE)).andReturn(singletonStatements1);

        replay(expressionFactory);

        String singletonStatements = singletonStatementsFactory.create(queryDto, ENTITY_TYPE);
        assertThat("Wrong number of singleton statements for Patient.", StringUtils.countMatches(CqlTestHelper.trim(singletonStatements), EXPECTED_SINGLETON_STATEMENTS_1), is(1));
        assertThat("Wrong number of singleton statements for Observation.", StringUtils.countMatches(CqlTestHelper.trim(singletonStatements), EXPECTED_SINGLETON_STATEMENTS_2), is(1));
    }

    @Test
    void test_create_WithTwoDifferentSingletonsInTwoEntities() {
        SimpleQueryDto queryDto = createDtoWithTwoFields(MDR_URN_1, MDR_URN_2);

        Set<CqlConfig.Singleton> singletonStatements1 =
                createExpectedSingletonStatementsList(createExpectedSingleton(SINGLETON_NAME_1));
        expect(expressionFactory.getSingletons(MDR_URN_1, ENTITY_TYPE)).andReturn(singletonStatements1);

        Set<CqlConfig.Singleton> singletonStatements2 =
                createExpectedSingletonStatementsList(createExpectedSingleton(SINGLETON_NAME_2));
        expect(expressionFactory.getSingletons(MDR_URN_2, ENTITY_TYPE)).andReturn(singletonStatements2);

        replay(expressionFactory);

        String singletonStatements = singletonStatementsFactory.create(queryDto, ENTITY_TYPE);
        assertThat("Wrong number of singleton statements for Patient.", StringUtils.countMatches(CqlTestHelper.trim(singletonStatements), EXPECTED_SINGLETON_STATEMENTS_1), is(1));
        assertThat("Wrong number of singleton statements for Observation.", StringUtils.countMatches(CqlTestHelper.trim(singletonStatements), EXPECTED_SINGLETON_STATEMENTS_2), is(1));
    }

    @Test
    void test_create_WithTheSameSingletonInTwoEntities() {
        SimpleQueryDto queryDto = createDtoWithTwoFields(MDR_URN_1, MDR_URN_2);

        Set<CqlConfig.Singleton> singletonStatements1 =
                createExpectedSingletonStatementsList(createExpectedSingleton(SINGLETON_NAME_1));
        expect(expressionFactory.getSingletons(MDR_URN_1, ENTITY_TYPE)).andReturn(singletonStatements1);

        Set<CqlConfig.Singleton> singletonStatements2 =
                createExpectedSingletonStatementsList(createExpectedSingleton(SINGLETON_NAME_1));
        expect(expressionFactory.getSingletons(MDR_URN_2, ENTITY_TYPE)).andReturn(singletonStatements2);

        replay(expressionFactory);

        String singletonStatements = singletonStatementsFactory.create(queryDto, ENTITY_TYPE);
        assertThat("Error creating singleton statements for two fields in two entities with the same singleton.", CqlTestHelper.trim(singletonStatements), is(EXPECTED_SINGLETON_STATEMENTS_1));
    }

    @Test
    void test_create_WithTheSameSingletonTwice() {
        SimpleQueryDto queryDto = createDtoWithFields(MDR_URN_1, MDR_URN_2);

        Set<CqlConfig.Singleton> singletonStatements1 =
                createExpectedSingletonStatementsList(createExpectedSingleton(SINGLETON_NAME_1));
        expect(expressionFactory.getSingletons(MDR_URN_1, ENTITY_TYPE)).andReturn(singletonStatements1);

        Set<CqlConfig.Singleton> singletonStatements2 =
                createExpectedSingletonStatementsList(createExpectedSingleton(SINGLETON_NAME_1));
        expect(expressionFactory.getSingletons(MDR_URN_2, ENTITY_TYPE)).andReturn(singletonStatements2);

        replay(expressionFactory);

        String singletonStatements = singletonStatementsFactory.create(queryDto, ENTITY_TYPE);
        assertThat("Error creating singleton statements for two fields with the same singleton.", CqlTestHelper.trim(singletonStatements), is(EXPECTED_SINGLETON_STATEMENTS_1));
    }

    @NotNull
    private SimpleQueryDto createDtoWithFields(String... urnList) {
        List<FieldDecimalDto> fieldDtos = new ArrayList<>();

        for (String mdrUrn : urnList) {
            ValueDecimalDto valueDto = CqlTestHelper.createValueDto(11.0, 47.0);
            FieldDecimalDto fieldDto = CqlTestHelper.createFieldDto(mdrUrn, valueDto);
            fieldDtos.add(fieldDto);
        }

        return CqlTestHelper.createSimpleQueryDto(fieldDtos.toArray(new FieldDecimalDto[]{}));
    }

    @NotNull
    private SimpleQueryDto createDtoWithTwoFields(String mdrUrn1, String mdrUrn2) {
        ValueDecimalDto valueDto1 = CqlTestHelper.createValueDto(11.0, 47.0);
        FieldDecimalDto fieldDto1 = CqlTestHelper.createFieldDto(mdrUrn1, valueDto1);

        ValueDecimalDto valueDto2 = CqlTestHelper.createValueDto(11.0, 47.0);
        FieldDecimalDto fieldDto2 = CqlTestHelper.createFieldDto(mdrUrn2, valueDto2);

        return CqlTestHelper.createSimpleQueryDtoTwoFieldsInTwoEntities(fieldDto1, fieldDto2);
    }

    @NotNull
    private Set<CqlConfig.Singleton> createExpectedSingletonStatementsList(CqlConfig.Singleton... singletons) {
        return new HashSet<>(Arrays.asList(singletons));
    }

    private CqlConfig.Singleton createExpectedSingleton(String singletonName) {
        CqlConfig.Singleton singleton = new CqlConfig.Singleton();
        singleton.setName(singletonName);
        return singleton;
    }
}
