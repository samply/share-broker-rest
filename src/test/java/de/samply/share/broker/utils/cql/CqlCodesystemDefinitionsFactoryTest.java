package de.samply.share.broker.utils.cql;

import de.samply.share.query.entity.SimpleQueryDto;
import de.samply.share.query.field.FieldDecimalDto;
import de.samply.share.query.value.ValueDecimalDto;
import org.apache.commons.lang.StringUtils;
import org.easymock.EasyMock;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.*;
import static org.easymock.EasyMock.*;
import static org.hamcrest.CoreMatchers.is;

import static de.samply.share.broker.utils.cql.CqlTestHelper.*;

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

        String codesystemDefinitions = codesytemDefinitionsFactory.create(new SimpleQueryDto());
        assertThat("Error creating codesystem definition for empty DTO.", CqlTestHelper.trim(codesystemDefinitions), is(""));
    }

    @Test
    void test_create_WithoutCodesystem() {
        SimpleQueryDto queryDto = createDtoWithOneField(MDR_URN_1);

        expect(expressionFactory.getCodesystemName(MDR_URN_1)).andReturn("");
        expect(expressionFactory.getCodesystemUrl(MDR_URN_1)).andReturn("");

        replay(expressionFactory);

        String codesystemDefinitions = codesytemDefinitionsFactory.create(queryDto);
        assertThat("Error creating codesystem definition for one field without codesytem.", CqlTestHelper.trim(codesystemDefinitions), is(""));
    }

    @Test
    void test_create_WithoutCodesystemName() {
        SimpleQueryDto queryDto = createDtoWithOneField(MDR_URN_1);

        expect(expressionFactory.getCodesystemName(MDR_URN_1)).andReturn("");
        expect(expressionFactory.getCodesystemUrl(MDR_URN_1)).andReturn(CODESYSTEM_URL_1);

        replay(expressionFactory);

        String codesystemDefinitions = codesytemDefinitionsFactory.create(queryDto);
        assertThat("Error creating codesystem definition  for one field without codesytem name.", CqlTestHelper.trim(codesystemDefinitions), is(""));
    }

    @Test
    void test_create_WithoutCodesystemUrl() {
        SimpleQueryDto queryDto = createDtoWithOneField(MDR_URN_1);

        expect(expressionFactory.getCodesystemName(MDR_URN_1)).andReturn(CODESYSTEM_NAME_1);
        expect(expressionFactory.getCodesystemUrl(MDR_URN_1)).andReturn("");

        replay(expressionFactory);

        String codesystemDefinitions = codesytemDefinitionsFactory.create(queryDto);
        assertThat("Error creating codesystem definition  for one field without codesytem url.", CqlTestHelper.trim(codesystemDefinitions), is(""));
    }

    @Test
    void test_create_WithCodesystem() {
        SimpleQueryDto queryDto = createDtoWithOneField(MDR_URN_1);

        expect(expressionFactory.getCodesystemName(MDR_URN_1)).andReturn(CODESYSTEM_NAME_1);
        expect(expressionFactory.getCodesystemUrl(MDR_URN_1)).andReturn(CODESYSTEM_URL_1);

        replay(expressionFactory);

        String codesystemDefinitions = codesytemDefinitionsFactory.create(queryDto);
        assertThat("Error creating codesystem definition  for one field with codesytem.", CqlTestHelper.trim(codesystemDefinitions), is(EXPECTED_CODESYTEM_DEFINITION_1));
    }

    @Test
    void test_create_WithTwoDifferentCodesystems() {
        SimpleQueryDto queryDto = createDtoWithOneField(MDR_URN_1, MDR_URN_2);

        expect(expressionFactory.getCodesystemName(MDR_URN_1)).andReturn(CODESYSTEM_NAME_1);
        expect(expressionFactory.getCodesystemUrl(MDR_URN_1)).andReturn(CODESYSTEM_URL_1);

        expect(expressionFactory.getCodesystemName(MDR_URN_2)).andReturn(CODESYSTEM_NAME_2);
        expect(expressionFactory.getCodesystemUrl(MDR_URN_2)).andReturn(CODESYSTEM_URL_2);

        replay(expressionFactory);

        String codesystemDefinitions = codesytemDefinitionsFactory.create(queryDto);
        assertThat("Wrong number of codesystem definitions for SampleMaterialType.", StringUtils.countMatches(CqlTestHelper.trim(codesystemDefinitions), EXPECTED_CODESYTEM_DEFINITION_1), is(1));
        assertThat("Wrong number of codesystem definitions for StorageTemperature.", StringUtils.countMatches(CqlTestHelper.trim(codesystemDefinitions), EXPECTED_CODESYTEM_DEFINITION_2), is(1));
    }

    @Test
    void test_create_WithTwoDifferentCodesystemsInTwoEntities() {
        SimpleQueryDto queryDto = createDtoWithTwoFields(MDR_URN_1, MDR_URN_2);

        expect(expressionFactory.getCodesystemName(MDR_URN_1)).andReturn(CODESYSTEM_NAME_1);
        expect(expressionFactory.getCodesystemUrl(MDR_URN_1)).andReturn(CODESYSTEM_URL_1);

        expect(expressionFactory.getCodesystemName(MDR_URN_2)).andReturn(CODESYSTEM_NAME_2);
        expect(expressionFactory.getCodesystemUrl(MDR_URN_2)).andReturn(CODESYSTEM_URL_2);

        replay(expressionFactory);

        String codesystemDefinitions = codesytemDefinitionsFactory.create(queryDto);
        assertThat("Wrong number of codesystem definitions for SampleMaterialType.", StringUtils.countMatches(CqlTestHelper.trim(codesystemDefinitions), EXPECTED_CODESYTEM_DEFINITION_1), is(1));
        assertThat("Wrong number of codesystem definitions for StorageTemperature.", StringUtils.countMatches(CqlTestHelper.trim(codesystemDefinitions), EXPECTED_CODESYTEM_DEFINITION_2), is(1));
    }

    @Test
    void test_create_WithTheSameCodesystemsInTwoEntities() {
        SimpleQueryDto queryDto = createDtoWithTwoFields(MDR_URN_1, MDR_URN_2);

        expect(expressionFactory.getCodesystemName(MDR_URN_1)).andReturn(CODESYSTEM_NAME_1);
        expect(expressionFactory.getCodesystemUrl(MDR_URN_1)).andReturn(CODESYSTEM_URL_1);

        expect(expressionFactory.getCodesystemName(MDR_URN_2)).andReturn(CODESYSTEM_NAME_1);
        expect(expressionFactory.getCodesystemUrl(MDR_URN_2)).andReturn(CODESYSTEM_URL_1);

        replay(expressionFactory);

        String codesystemDefinitions = codesytemDefinitionsFactory.create(queryDto);
        assertThat("Error creating codesystem definition for two fields in two entities with the same codesytem.", CqlTestHelper.trim(codesystemDefinitions), is(EXPECTED_CODESYTEM_DEFINITION_1));
    }

    @Test
    void test_create_WithSameCodesystemTwice() {
        SimpleQueryDto queryDto = createDtoWithOneField(MDR_URN_1, MDR_URN_2);

        expect(expressionFactory.getCodesystemName(MDR_URN_1)).andReturn(CODESYSTEM_NAME_1);
        expect(expressionFactory.getCodesystemUrl(MDR_URN_1)).andReturn(CODESYSTEM_URL_1);

        expect(expressionFactory.getCodesystemName(MDR_URN_2)).andReturn(CODESYSTEM_NAME_1);
        expect(expressionFactory.getCodesystemUrl(MDR_URN_2)).andReturn(CODESYSTEM_URL_1);

        replay(expressionFactory);

        String codesystemDefinitions = codesytemDefinitionsFactory.create(queryDto);
        assertThat("Error creating codesystem definition for two fields with the same codesytem.", CqlTestHelper.trim(codesystemDefinitions), is(EXPECTED_CODESYTEM_DEFINITION_1));
    }

    @NotNull
    private SimpleQueryDto createDtoWithOneField(String... urnList) {
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
}
