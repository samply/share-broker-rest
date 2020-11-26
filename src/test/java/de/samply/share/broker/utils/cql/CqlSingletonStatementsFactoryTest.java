package de.samply.share.broker.utils.cql;

import static de.samply.share.broker.utils.cql.CqlTestHelper.ENTITY_TYPE;
import static de.samply.share.broker.utils.cql.CqlTestHelper.MDR_URN_1;
import static de.samply.share.broker.utils.cql.CqlTestHelper.MDR_URN_2;
import static de.samply.share.broker.utils.cql.CqlTestHelper.SINGLETON_NAME_1;
import static de.samply.share.broker.utils.cql.CqlTestHelper.SINGLETON_NAME_2;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import de.samply.share.essentialquery.EssentialSimpleFieldDto;
import de.samply.share.essentialquery.EssentialSimpleQueryDto;
import de.samply.share.essentialquery.EssentialSimpleValueDto;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.easymock.EasyMock;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CqlSingletonStatementsFactoryTest {

  private static final String EXPECTED_SINGLETON_STATEMENTS_1 =
      "define " + SINGLETON_NAME_1 + ": singleton from([" + SINGLETON_NAME_1 + "])";
  private static final String EXPECTED_SINGLETON_STATEMENTS_2 =
      "define " + SINGLETON_NAME_2 + ": singleton from([" + SINGLETON_NAME_2 + "])";

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

    String singletonStatements = singletonStatementsFactory
        .create(new EssentialSimpleQueryDto(), ENTITY_TYPE);
    assertThat("Error creating singleton statements for empty DTO.",
        CqlTestHelper.trim(singletonStatements), is(""));
  }

  @Test
  void test_create_WithoutSingleton() {
    EssentialSimpleQueryDto queryDto = createDtoWithFields(MDR_URN_1);

    expect(expressionFactory.getSingletons(MDR_URN_1, ENTITY_TYPE)).andReturn(new HashSet<>());

    replay(expressionFactory);

    String singletonStatements = singletonStatementsFactory.create(queryDto, ENTITY_TYPE);
    assertThat("Error creating singleton statements for one field without singleton.",
        CqlTestHelper.trim(singletonStatements), is(""));
  }

  @Test
  void test_create_WithSingleton() {
    EssentialSimpleQueryDto queryDto = createDtoWithFields(MDR_URN_1);

    Set<CqlConfig.Singleton> singletonStatements1 =
        createExpectedSingletonStatementsList(createExpectedSingleton(SINGLETON_NAME_1));
    expect(expressionFactory.getSingletons(MDR_URN_1, ENTITY_TYPE)).andReturn(singletonStatements1);

    replay(expressionFactory);

    String singletonStatements = singletonStatementsFactory.create(queryDto, ENTITY_TYPE);
    assertThat("Error creating singleton statements for one field with singleton.",
        CqlTestHelper.trim(singletonStatements), is(EXPECTED_SINGLETON_STATEMENTS_1));
  }

  @Test
  void test_create_WithTwoDifferentSingletons() {
    EssentialSimpleQueryDto queryDto = createDtoWithFields(MDR_URN_1, MDR_URN_2);

    Set<CqlConfig.Singleton> singletonStatements1 =
        createExpectedSingletonStatementsList(createExpectedSingleton(SINGLETON_NAME_1));
    expect(expressionFactory.getSingletons(MDR_URN_1, ENTITY_TYPE)).andReturn(singletonStatements1);

    Set<CqlConfig.Singleton> singletonStatements2 =
        createExpectedSingletonStatementsList(createExpectedSingleton(SINGLETON_NAME_2));
    expect(expressionFactory.getSingletons(MDR_URN_2, ENTITY_TYPE)).andReturn(singletonStatements2);

    replay(expressionFactory);

    String singletonStatements = singletonStatementsFactory.create(queryDto, ENTITY_TYPE);
    assertThat("Wrong number of singleton statements for Patient.", StringUtils
            .countMatches(CqlTestHelper.trim(singletonStatements), EXPECTED_SINGLETON_STATEMENTS_1),
        is(1));
    assertThat("Wrong number of singleton statements for Observation.", StringUtils
            .countMatches(CqlTestHelper.trim(singletonStatements), EXPECTED_SINGLETON_STATEMENTS_2),
        is(1));
  }

  @Test
  void test_create_WithTwoDifferentSingletonsInOneField() {
    EssentialSimpleQueryDto queryDto = createDtoWithFields(MDR_URN_1);

    Set<CqlConfig.Singleton> singletonStatements1 =
        createExpectedSingletonStatementsList(
            createExpectedSingleton(SINGLETON_NAME_1),
            createExpectedSingleton(SINGLETON_NAME_2));
    expect(expressionFactory.getSingletons(MDR_URN_1, ENTITY_TYPE)).andReturn(singletonStatements1);

    replay(expressionFactory);

    String singletonStatements = singletonStatementsFactory.create(queryDto, ENTITY_TYPE);
    assertThat("Wrong number of singleton statements for Patient.", StringUtils
            .countMatches(CqlTestHelper.trim(singletonStatements), EXPECTED_SINGLETON_STATEMENTS_1),
        is(1));
    assertThat("Wrong number of singleton statements for Observation.", StringUtils
            .countMatches(CqlTestHelper.trim(singletonStatements), EXPECTED_SINGLETON_STATEMENTS_2),
        is(1));
  }

  @Test
  void test_create_WithTwoDifferentSingletonsInTwoEntities() {
    EssentialSimpleQueryDto queryDto = createDtoWithTwoFields(MDR_URN_1, MDR_URN_2);

    Set<CqlConfig.Singleton> singletonStatements1 =
        createExpectedSingletonStatementsList(createExpectedSingleton(SINGLETON_NAME_1));
    expect(expressionFactory.getSingletons(MDR_URN_1, ENTITY_TYPE)).andReturn(singletonStatements1);

    Set<CqlConfig.Singleton> singletonStatements2 =
        createExpectedSingletonStatementsList(createExpectedSingleton(SINGLETON_NAME_2));
    expect(expressionFactory.getSingletons(MDR_URN_2, ENTITY_TYPE)).andReturn(singletonStatements2);

    replay(expressionFactory);

    String singletonStatements = singletonStatementsFactory.create(queryDto, ENTITY_TYPE);
    assertThat("Wrong number of singleton statements for Patient.", StringUtils
            .countMatches(CqlTestHelper.trim(singletonStatements), EXPECTED_SINGLETON_STATEMENTS_1),
        is(1));
    assertThat("Wrong number of singleton statements for Observation.", StringUtils
            .countMatches(CqlTestHelper.trim(singletonStatements), EXPECTED_SINGLETON_STATEMENTS_2),
        is(1));
  }

  @Test
  void test_create_WithTheSameSingletonInTwoEntities() {
    EssentialSimpleQueryDto queryDto = createDtoWithTwoFields(MDR_URN_1, MDR_URN_2);

    Set<CqlConfig.Singleton> singletonStatements1 =
        createExpectedSingletonStatementsList(createExpectedSingleton(SINGLETON_NAME_1));
    expect(expressionFactory.getSingletons(MDR_URN_1, ENTITY_TYPE)).andReturn(singletonStatements1);

    Set<CqlConfig.Singleton> singletonStatements2 =
        createExpectedSingletonStatementsList(createExpectedSingleton(SINGLETON_NAME_1));
    expect(expressionFactory.getSingletons(MDR_URN_2, ENTITY_TYPE)).andReturn(singletonStatements2);

    replay(expressionFactory);

    String singletonStatements = singletonStatementsFactory.create(queryDto, ENTITY_TYPE);
    assertThat(
        "Error creating singleton statements for two fields in two entities with the same singleton.",
        CqlTestHelper.trim(singletonStatements), is(EXPECTED_SINGLETON_STATEMENTS_1));
  }

  @Test
  void test_create_WithTheSameSingletonTwice() {
    EssentialSimpleQueryDto queryDto = createDtoWithFields(MDR_URN_1, MDR_URN_2);

    Set<CqlConfig.Singleton> singletonStatements1 =
        createExpectedSingletonStatementsList(createExpectedSingleton(SINGLETON_NAME_1));
    expect(expressionFactory.getSingletons(MDR_URN_1, ENTITY_TYPE)).andReturn(singletonStatements1);

    Set<CqlConfig.Singleton> singletonStatements2 =
        createExpectedSingletonStatementsList(createExpectedSingleton(SINGLETON_NAME_1));
    expect(expressionFactory.getSingletons(MDR_URN_2, ENTITY_TYPE)).andReturn(singletonStatements2);

    replay(expressionFactory);

    String singletonStatements = singletonStatementsFactory.create(queryDto, ENTITY_TYPE);
    assertThat("Error creating singleton statements for two fields with the same singleton.",
        CqlTestHelper.trim(singletonStatements), is(EXPECTED_SINGLETON_STATEMENTS_1));
  }

  @NotNull
  private EssentialSimpleQueryDto createDtoWithFields(String... urnList) {
    List<EssentialSimpleFieldDto> fieldDtos = new ArrayList<>();

    for (String mdrUrn : urnList) {
      EssentialSimpleValueDto valueDto = CqlTestHelper.createValueDto(11.0, 47.0);
      EssentialSimpleFieldDto fieldDto = CqlTestHelper.createFieldDto(mdrUrn, valueDto);
      fieldDtos.add(fieldDto);
    }

    return CqlTestHelper.createSimpleQueryDto(fieldDtos.toArray(new EssentialSimpleFieldDto[]{}));
  }

  @NotNull
  private EssentialSimpleQueryDto createDtoWithTwoFields(String mdrUrn1, String mdrUrn2) {
    EssentialSimpleValueDto valueDto1 = CqlTestHelper.createValueDto(11.0, 47.0);
    EssentialSimpleFieldDto fieldDto1 = CqlTestHelper.createFieldDto(mdrUrn1, valueDto1);

    EssentialSimpleValueDto valueDto2 = CqlTestHelper.createValueDto(11.0, 47.0);
    EssentialSimpleFieldDto fieldDto2 = CqlTestHelper.createFieldDto(mdrUrn2, valueDto2);

    return CqlTestHelper.createSimpleQueryDto(fieldDto1, fieldDto2);
  }

  @NotNull
  private Set<CqlConfig.Singleton> createExpectedSingletonStatementsList(
      CqlConfig.Singleton... singletons) {
    return new HashSet<>(Arrays.asList(singletons));
  }

  private CqlConfig.Singleton createExpectedSingleton(String singletonName) {
    CqlConfig.Singleton singleton = new CqlConfig.Singleton();
    singleton.setName(singletonName);
    return singleton;
  }
}
