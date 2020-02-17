package de.samply.share.broker.utils.cql;

import de.samply.share.essentialquery.EssentialSimpleFieldDto;
import de.samply.share.essentialquery.EssentialSimpleQueryDto;
import de.samply.share.essentialquery.EssentialSimpleValueDto;
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

class CqlCodesystemDefinitionsFactoryTest {

    private static final String EXPECTED_CODESYTEM_DEFINITION_1 = "codesystem " + CODESYSTEM_NAME_1 + ": '" + CODESYSTEM_URL_1 + "'";
    private static final String EXPECTED_CODESYTEM_DEFINITION_2 = "codesystem " + CODESYSTEM_NAME_2 + ": '" + CODESYSTEM_URL_2 + "'";

    private CqlExpressionFactory expressionFactory;
    private CqlCodesytemDefinitionsFactory codesytemDefinitionsFactory;

    @BeforeEach
    void initFactory() {
        expressionFactory = EasyMock.createNiceMock(CqlExpressionFactory.class);

        codesytemDefinitionsFactory = new CqlCodesytemDefinitionsFactory(expressionFactory);
    }

    @Test
    void test_create_emptyDto() {
        replay(expressionFactory);

        String codesystemDefinitions = codesytemDefinitionsFactory.create(new EssentialSimpleQueryDto());
        assertThat("Error creating codesystem definition for empty DTO.", CqlTestHelper.trim(codesystemDefinitions), is(""));
    }

    @Test
    void test_create_WithoutCodesystem() {
        EssentialSimpleQueryDto queryDto = createDtoWithFields(MDR_URN_1);

        expect(expressionFactory.getCodesystems(MDR_URN_1)).andReturn(new HashSet<>());

        replay(expressionFactory);

        String codesystemDefinitions = codesytemDefinitionsFactory.create(queryDto);
        assertThat("Error creating codesystem definition for one field without codesytem.", CqlTestHelper.trim(codesystemDefinitions), is(""));
    }

    @Test
    void test_create_WithCodesystem() {
        EssentialSimpleQueryDto queryDto = createDtoWithFields(MDR_URN_1);

        Set<CqlConfig.Codesystem> expectedCodesystems1 =
                createExpectedCodesystemList(createExpectedCodesystem(CODESYSTEM_NAME_1, CODESYSTEM_URL_1));
        expect(expressionFactory.getCodesystems(MDR_URN_1)).andReturn(expectedCodesystems1);

        replay(expressionFactory);

        String codesystemDefinitions = codesytemDefinitionsFactory.create(queryDto);
        assertThat("Error creating codesystem definition  for one field with codesytem.", CqlTestHelper.trim(codesystemDefinitions), is(EXPECTED_CODESYTEM_DEFINITION_1));
    }

    @Test
    void test_create_WithTwoDifferentCodesystems() {
        EssentialSimpleQueryDto queryDto = createDtoWithFields(MDR_URN_1, MDR_URN_2);

        Set<CqlConfig.Codesystem> expectedCodesystems1 =
                createExpectedCodesystemList(createExpectedCodesystem(CODESYSTEM_NAME_1, CODESYSTEM_URL_1));
        expect(expressionFactory.getCodesystems(MDR_URN_1)).andReturn(expectedCodesystems1);

        Set<CqlConfig.Codesystem> expectedCodesystems2 =
                createExpectedCodesystemList(createExpectedCodesystem(CODESYSTEM_NAME_2, CODESYSTEM_URL_2));
        expect(expressionFactory.getCodesystems(MDR_URN_2)).andReturn(expectedCodesystems2);

        replay(expressionFactory);

        String codesystemDefinitions = codesytemDefinitionsFactory.create(queryDto);
        assertThat("Wrong number of codesystem definitions for SampleMaterialType.", StringUtils.countMatches(CqlTestHelper.trim(codesystemDefinitions), EXPECTED_CODESYTEM_DEFINITION_1), is(1));
        assertThat("Wrong number of codesystem definitions for StorageTemperature.", StringUtils.countMatches(CqlTestHelper.trim(codesystemDefinitions), EXPECTED_CODESYTEM_DEFINITION_2), is(1));
    }

    @Test
    void test_create_WithTwoDifferentCodesystemsInOneField() {
        EssentialSimpleQueryDto queryDto = createDtoWithFields(MDR_URN_1);

        Set<CqlConfig.Codesystem> expectedCodesystems1 =
                createExpectedCodesystemList(
                        createExpectedCodesystem(CODESYSTEM_NAME_1, CODESYSTEM_URL_1),
                        createExpectedCodesystem(CODESYSTEM_NAME_2, CODESYSTEM_URL_2));
        expect(expressionFactory.getCodesystems(MDR_URN_1)).andReturn(expectedCodesystems1);

        replay(expressionFactory);

        String codesystemDefinitions = codesytemDefinitionsFactory.create(queryDto);
        assertThat("Wrong number of codesystem definitions for SampleMaterialType.", StringUtils.countMatches(CqlTestHelper.trim(codesystemDefinitions), EXPECTED_CODESYTEM_DEFINITION_1), is(1));
        assertThat("Wrong number of codesystem definitions for StorageTemperature.", StringUtils.countMatches(CqlTestHelper.trim(codesystemDefinitions), EXPECTED_CODESYTEM_DEFINITION_2), is(1));
    }

    @Test
    void test_create_WithTwoDifferentCodesystemsInTwoEntities() {
        EssentialSimpleQueryDto queryDto = createDtoWithTwoFields(MDR_URN_1, MDR_URN_2);

        Set<CqlConfig.Codesystem> expectedCodesystems1 =
                createExpectedCodesystemList(createExpectedCodesystem(CODESYSTEM_NAME_1, CODESYSTEM_URL_1));
        expect(expressionFactory.getCodesystems(MDR_URN_1)).andReturn(expectedCodesystems1);

        Set<CqlConfig.Codesystem> expectedCodesystems2 =
                createExpectedCodesystemList(createExpectedCodesystem(CODESYSTEM_NAME_2, CODESYSTEM_URL_2));
        expect(expressionFactory.getCodesystems(MDR_URN_2)).andReturn(expectedCodesystems2);

        replay(expressionFactory);

        String codesystemDefinitions = codesytemDefinitionsFactory.create(queryDto);
        assertThat("Wrong number of codesystem definitions for SampleMaterialType.", StringUtils.countMatches(CqlTestHelper.trim(codesystemDefinitions), EXPECTED_CODESYTEM_DEFINITION_1), is(1));
        assertThat("Wrong number of codesystem definitions for StorageTemperature.", StringUtils.countMatches(CqlTestHelper.trim(codesystemDefinitions), EXPECTED_CODESYTEM_DEFINITION_2), is(1));
    }

    @Test
    void test_create_WithTheSameCodesystemsInTwoEntities() {
        EssentialSimpleQueryDto queryDto = createDtoWithTwoFields(MDR_URN_1, MDR_URN_2);

        Set<CqlConfig.Codesystem> expectedCodesystems1 =
                createExpectedCodesystemList(createExpectedCodesystem(CODESYSTEM_NAME_1, CODESYSTEM_URL_1));
        expect(expressionFactory.getCodesystems(MDR_URN_1)).andReturn(expectedCodesystems1);

        Set<CqlConfig.Codesystem> expectedCodesystems2 =
                createExpectedCodesystemList(createExpectedCodesystem(CODESYSTEM_NAME_1, CODESYSTEM_URL_1));
        expect(expressionFactory.getCodesystems(MDR_URN_2)).andReturn(expectedCodesystems2);

        replay(expressionFactory);

        String codesystemDefinitions = codesytemDefinitionsFactory.create(queryDto);
        assertThat("Error creating codesystem definition for two fields in two entities with the same codesytem.", CqlTestHelper.trim(codesystemDefinitions), is(EXPECTED_CODESYTEM_DEFINITION_1));
    }

    @Test
    void test_create_WithTheSameCodesystemTwice() {
        EssentialSimpleQueryDto queryDto = createDtoWithFields(MDR_URN_1, MDR_URN_2);

        Set<CqlConfig.Codesystem> expectedCodesystems1 =
                createExpectedCodesystemList(createExpectedCodesystem(CODESYSTEM_NAME_1, CODESYSTEM_URL_1));
        expect(expressionFactory.getCodesystems(MDR_URN_1)).andReturn(expectedCodesystems1);

        Set<CqlConfig.Codesystem> expectedCodesystems2 =
                createExpectedCodesystemList(createExpectedCodesystem(CODESYSTEM_NAME_1, CODESYSTEM_URL_1));
        expect(expressionFactory.getCodesystems(MDR_URN_2)).andReturn(expectedCodesystems2);

        replay(expressionFactory);

        String codesystemDefinitions = codesytemDefinitionsFactory.create(queryDto);
        assertThat("Error creating codesystem definition for two fields with the same codesytem.", CqlTestHelper.trim(codesystemDefinitions), is(EXPECTED_CODESYTEM_DEFINITION_1));
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

    @NotNull
    private EssentialSimpleQueryDto createDtoWithTwoFields(String mdrUrn1, String mdrUrn2) {
        EssentialSimpleValueDto valueDto1 = CqlTestHelper.createValueDto(11.0, 47.0);
        EssentialSimpleFieldDto fieldDto1 = createFieldDto(mdrUrn1, valueDto1);

        EssentialSimpleValueDto valueDto2 = CqlTestHelper.createValueDto(11.0, 47.0);
        EssentialSimpleFieldDto fieldDto2 = CqlTestHelper.createFieldDto(mdrUrn2, valueDto2);

        return CqlTestHelper.createSimpleQueryDto(fieldDto1, fieldDto2);
    }

    @NotNull
    private Set<CqlConfig.Codesystem> createExpectedCodesystemList(CqlConfig.Codesystem... codsystems) {
        return new HashSet<>(Arrays.asList(codsystems));
    }

    private CqlConfig.Codesystem createExpectedCodesystem(String codesystemName, String codesystemUrl) {
        CqlConfig.Codesystem codesystem = new CqlConfig.Codesystem();
        codesystem.setName(codesystemName);
        codesystem.setUrl(codesystemUrl);
        return codesystem;
    }
}
