package de.samply.share.broker.utils.cql;

import static de.samply.share.broker.utils.cql.CqlTestHelper.CODESYSTEM_NAME_1;
import static de.samply.share.broker.utils.cql.CqlTestHelper.CODESYSTEM_NAME_2;
import static de.samply.share.broker.utils.cql.CqlTestHelper.CODESYSTEM_URL_1;
import static de.samply.share.broker.utils.cql.CqlTestHelper.CODESYSTEM_URL_2;
import static de.samply.share.broker.utils.cql.CqlTestHelper.MDR_URN_1;
import static de.samply.share.broker.utils.cql.CqlTestHelper.MDR_URN_2;
import static de.samply.share.broker.utils.cql.CqlTestHelper.createFieldDto;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import com.google.common.collect.ImmutableSet;
import de.samply.share.essentialquery.EssentialSimpleFieldDto;
import de.samply.share.essentialquery.EssentialSimpleQueryDto;
import de.samply.share.essentialquery.EssentialSimpleValueDto;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.easymock.EasyMock;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CqlCodeSystemDefinitionsFactoryTest {

  private static final String EXPECTED_CODESYSTEM_DEFINITION_1 =
      "codesystem " + CODESYSTEM_NAME_1 + ": '" + CODESYSTEM_URL_1 + "'";
  private static final String EXPECTED_CODESYSTEM_DEFINITION_2 =
      "codesystem " + CODESYSTEM_NAME_2 + ": '" + CODESYSTEM_URL_2 + "'";

  private CqlExpressionFactory expressionFactory;
  private CqlCodeSystemDefinitionsFactory codeSystemDefinitionsFactory;

  @BeforeEach
  void initFactory() {
    expressionFactory = EasyMock.createNiceMock(CqlExpressionFactory.class);

    codeSystemDefinitionsFactory = new CqlCodeSystemDefinitionsFactory(expressionFactory);
  }

  @Test
  void test_create_emptyDto() {
    replay(expressionFactory);

    String definitions = codeSystemDefinitionsFactory.create(new EssentialSimpleQueryDto());
    assertThat("Error creating code system definition for empty DTO.",
        CqlTestHelper.trim(definitions), is(""));
  }

  @Test
  void test_create_emptyDto_oneFixCodeSystem() {
    replay(expressionFactory);

    String definitions = codeSystemDefinitionsFactory.create(new EssentialSimpleQueryDto(),
        createCodeSystem(CODESYSTEM_NAME_1, CODESYSTEM_URL_1));
    assertThat(CqlTestHelper.trim(definitions), is(EXPECTED_CODESYSTEM_DEFINITION_1));
  }

  @Test
  void test_create_WithoutCodeSystem() {
    EssentialSimpleQueryDto queryDto = createDtoWithFields(MDR_URN_1);

    expect(expressionFactory.getCodesystems(MDR_URN_1)).andReturn(Collections.emptySet());

    replay(expressionFactory);

    String definitions = codeSystemDefinitionsFactory.create(queryDto);
    assertThat("Error creating code system definition for one field without code system.",
        CqlTestHelper.trim(definitions), is(""));
  }

  @Test
  void test_create_WithCodeSystem() {
    EssentialSimpleQueryDto queryDto = createDtoWithFields(MDR_URN_1);

    expect(expressionFactory.getCodesystems(MDR_URN_1)).andReturn(ImmutableSet.of(
        createCodeSystem(CODESYSTEM_NAME_1, CODESYSTEM_URL_1)));

    replay(expressionFactory);

    String definitions = codeSystemDefinitionsFactory.create(queryDto);
    assertThat("Error creating code system definition for one field with code system.",
        CqlTestHelper.trim(definitions), is(EXPECTED_CODESYSTEM_DEFINITION_1));
  }

  @Test
  void test_create_WithCodeSystem_oneFixCodeSystem() {
    EssentialSimpleQueryDto queryDto = createDtoWithFields(MDR_URN_1);

    expect(expressionFactory.getCodesystems(MDR_URN_1)).andReturn(ImmutableSet.of(
        createCodeSystem(CODESYSTEM_NAME_1, CODESYSTEM_URL_1)));

    replay(expressionFactory);

    String definitions = codeSystemDefinitionsFactory.create(queryDto,
        createCodeSystem(CODESYSTEM_NAME_2, CODESYSTEM_URL_2));
    assertThat("Wrong number of code system definitions for SampleMaterialType.",
        StringUtils.countMatches(CqlTestHelper.trim(definitions), EXPECTED_CODESYSTEM_DEFINITION_1),
        is(1));
    assertThat("Wrong number of code system definitions for StorageTemperature.",
        StringUtils.countMatches(CqlTestHelper.trim(definitions), EXPECTED_CODESYSTEM_DEFINITION_2),
        is(1));
  }

  @Test
  void test_create_WithSameCodeSystemFromQueryDtoAndFixCodeSystem() {
    EssentialSimpleQueryDto queryDto = createDtoWithFields(MDR_URN_1);

    expect(expressionFactory.getCodesystems(MDR_URN_1)).andReturn(ImmutableSet.of(
        createCodeSystem(CODESYSTEM_NAME_1, CODESYSTEM_URL_1)));

    replay(expressionFactory);

    String definitions = codeSystemDefinitionsFactory.create(queryDto,
        createCodeSystem(CODESYSTEM_NAME_1, CODESYSTEM_URL_1));
    assertThat("Error creating code system definition for one field with code system.",
        CqlTestHelper.trim(definitions), is(EXPECTED_CODESYSTEM_DEFINITION_1));
  }

  @Test
  void test_create_WithTwoDifferentCodeSystems() {
    EssentialSimpleQueryDto queryDto = createDtoWithFields(MDR_URN_1, MDR_URN_2);

    expect(expressionFactory.getCodesystems(MDR_URN_1)).andReturn(ImmutableSet.of(
        createCodeSystem(CODESYSTEM_NAME_1, CODESYSTEM_URL_1)));

    expect(expressionFactory.getCodesystems(MDR_URN_2)).andReturn(ImmutableSet.of(
        createCodeSystem(CODESYSTEM_NAME_2, CODESYSTEM_URL_2)));

    replay(expressionFactory);

    String definitions = codeSystemDefinitionsFactory.create(queryDto);
    assertThat("Wrong number of code system definitions for SampleMaterialType.",
        StringUtils.countMatches(CqlTestHelper.trim(definitions), EXPECTED_CODESYSTEM_DEFINITION_1),
        is(1));
    assertThat("Wrong number of code system definitions for StorageTemperature.",
        StringUtils.countMatches(CqlTestHelper.trim(definitions), EXPECTED_CODESYSTEM_DEFINITION_2),
        is(1));
  }

  @Test
  void test_create_WithTwoDifferentCodeSystemsInOneField() {
    EssentialSimpleQueryDto queryDto = createDtoWithFields(MDR_URN_1);

    expect(expressionFactory.getCodesystems(MDR_URN_1)).andReturn(ImmutableSet.of(
        createCodeSystem(CODESYSTEM_NAME_1, CODESYSTEM_URL_1),
        createCodeSystem(CODESYSTEM_NAME_2, CODESYSTEM_URL_2)));

    replay(expressionFactory);

    String definitions = codeSystemDefinitionsFactory.create(queryDto);
    assertThat("Wrong number of code system definitions for SampleMaterialType.",
        StringUtils.countMatches(CqlTestHelper.trim(definitions), EXPECTED_CODESYSTEM_DEFINITION_1),
        is(1));
    assertThat("Wrong number of code system definitions for StorageTemperature.",
        StringUtils.countMatches(CqlTestHelper.trim(definitions), EXPECTED_CODESYSTEM_DEFINITION_2),
        is(1));
  }

  @Test
  void test_create_WithTwoDifferentCodeSystemsInTwoEntities() {
    EssentialSimpleQueryDto queryDto = createDtoWithFields(MDR_URN_1, MDR_URN_2);

    expect(expressionFactory.getCodesystems(MDR_URN_1)).andReturn(ImmutableSet.of(
        createCodeSystem(CODESYSTEM_NAME_1, CODESYSTEM_URL_1)));

    expect(expressionFactory.getCodesystems(MDR_URN_2)).andReturn(ImmutableSet.of(
        createCodeSystem(CODESYSTEM_NAME_2, CODESYSTEM_URL_2)));

    replay(expressionFactory);

    String definitions = codeSystemDefinitionsFactory.create(queryDto);
    assertThat("Wrong number of code system definitions for SampleMaterialType.",
        StringUtils.countMatches(CqlTestHelper.trim(definitions), EXPECTED_CODESYSTEM_DEFINITION_1),
        is(1));
    assertThat("Wrong number of code system definitions for StorageTemperature.",
        StringUtils.countMatches(CqlTestHelper.trim(definitions), EXPECTED_CODESYSTEM_DEFINITION_2),
        is(1));
  }

  @Test
  void test_create_WithTheSameCodeSystemInTwoEntities() {
    EssentialSimpleQueryDto queryDto = createDtoWithFields(MDR_URN_1, MDR_URN_2);

    Set<CqlConfig.Codesystem> expectedCodeSystem =
        ImmutableSet.of(createCodeSystem(CODESYSTEM_NAME_1, CODESYSTEM_URL_1));
    expect(expressionFactory.getCodesystems(MDR_URN_1)).andReturn(expectedCodeSystem);
    expect(expressionFactory.getCodesystems(MDR_URN_2)).andReturn(expectedCodeSystem);

    replay(expressionFactory);

    String definitions = codeSystemDefinitionsFactory.create(queryDto);
    assertThat(
        "Error creating code system definition for two fields in two entities with the same code system.",
        CqlTestHelper.trim(definitions), is(EXPECTED_CODESYSTEM_DEFINITION_1));
  }

  @Test
  void test_create_WithEqualCodeSystemInTwoEntities() {
    EssentialSimpleQueryDto queryDto = createDtoWithFields(MDR_URN_1, MDR_URN_2);

    Set<CqlConfig.Codesystem> expectedCodeSystem1 =
        ImmutableSet.of(createCodeSystem(CODESYSTEM_NAME_1, CODESYSTEM_URL_1));
    expect(expressionFactory.getCodesystems(MDR_URN_1)).andReturn(expectedCodeSystem1);
    Set<CqlConfig.Codesystem> expectedCodeSystem2 =
        ImmutableSet.of(createCodeSystem(CODESYSTEM_NAME_1, CODESYSTEM_URL_1));
    expect(expressionFactory.getCodesystems(MDR_URN_2)).andReturn(expectedCodeSystem2);

    replay(expressionFactory);

    String definitions = codeSystemDefinitionsFactory.create(queryDto);
    assertThat(
        "Error creating code system definition for two fields in two entities with the same code system.",
        CqlTestHelper.trim(definitions), is(EXPECTED_CODESYSTEM_DEFINITION_1));
  }

  @NotNull
  private EssentialSimpleQueryDto createDtoWithFields(String... urnList) {
    List<EssentialSimpleFieldDto> fieldDtos = new ArrayList<>();

    for (String mdrUrn : urnList) {
      EssentialSimpleValueDto valueDto = CqlTestHelper.createValueDto(11.0, 47.0);
      EssentialSimpleFieldDto fieldDto = createFieldDto(mdrUrn, valueDto);
      fieldDtos.add(fieldDto);
    }

    return CqlTestHelper.createSimpleQueryDto(fieldDtos.toArray(new EssentialSimpleFieldDto[]{}));
  }

  private CqlConfig.Codesystem createCodeSystem(String name, String url) {
    CqlConfig.Codesystem codeSystem = new CqlConfig.Codesystem();
    codeSystem.setName(name);
    codeSystem.setUrl(url);
    return codeSystem;
  }
}
