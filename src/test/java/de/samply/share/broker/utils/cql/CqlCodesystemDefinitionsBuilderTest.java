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

class CqlCodesystemDefinitionsBuilderTest {

    private static final String EXPECTED_CODESYTEM_DEFINITION_1 = "codesystem " + CODESYSTEM_NAME_1 + ": '" + CODESYSTEM_URL_1 + "'";
    private static final String EXPECTED_CODESYTEM_DEFINITION_2 = "codesystem " + CODESYSTEM_NAME_2 + ": '" + CODESYSTEM_URL_2 + "'";

    private CqlExpressionFactory expressionFactory;
    private CqlCodesytesmDefinitionsBuilder codesytesmDefinitionsBuilder;

    @BeforeEach
    void initBuilder() {
        expressionFactory = EasyMock.createNiceMock(CqlExpressionFactory.class);

        codesytesmDefinitionsBuilder = new CqlCodesytesmDefinitionsBuilder(expressionFactory);
    }

    @Test
    void test_createCodesystemDefinitionsStatement_emptyDto() {
        replay(expressionFactory);

        String input = codesytesmDefinitionsBuilder.createCodesystemDefinitionsStatement(new SimpleQueryDto());

        assertThat("Error creating codesystem definition for empty DTO.", CqlTestHelper.trim(input), is(""));
    }

    @Test
    void test_createCodesystemDefinitionsStatement_WithoutCodesystem() {
        SimpleQueryDto queryDto = createDtoWithOneField(MDR_URN_1);

        expect(expressionFactory.getCodesystemName(MDR_URN_1)).andReturn("");
        expect(expressionFactory.getCodesystemUrl(MDR_URN_1)).andReturn("");

        replay(expressionFactory);

        assertThat("Error creating codesystem definition for one field without codesytem.", CqlTestHelper.trim(codesytesmDefinitionsBuilder.createCodesystemDefinitionsStatement(queryDto)), is(""));
    }

    @Test
    void test_createCodesystemDefinitionsStatement_WithoutCodesystemName() {
        SimpleQueryDto queryDto = createDtoWithOneField(MDR_URN_1);

        expect(expressionFactory.getCodesystemName(MDR_URN_1)).andReturn("");
        expect(expressionFactory.getCodesystemUrl(MDR_URN_1)).andReturn(CODESYSTEM_URL_1);

        replay(expressionFactory);

        assertThat("Error creating codesystem definition  for one field without codesytem name.", CqlTestHelper.trim(codesytesmDefinitionsBuilder.createCodesystemDefinitionsStatement(queryDto)), is(""));
    }

    @Test
    void test_createCodesystemDefinitionsStatement_WithoutCodesystemUrl() {
        SimpleQueryDto queryDto = createDtoWithOneField(MDR_URN_1);

        expect(expressionFactory.getCodesystemName(MDR_URN_1)).andReturn(CODESYSTEM_NAME_1);
        expect(expressionFactory.getCodesystemUrl(MDR_URN_1)).andReturn("");

        replay(expressionFactory);

        assertThat("Error creating codesystem definition  for one field without codesytem url.", CqlTestHelper.trim(codesytesmDefinitionsBuilder.createCodesystemDefinitionsStatement(queryDto)), is(""));
    }

    @Test
    void test_createCodesystemDefinitionsStatement_WithCodesystem() {
        SimpleQueryDto queryDto = createDtoWithOneField(MDR_URN_1);

        expect(expressionFactory.getCodesystemName(MDR_URN_1)).andReturn(CODESYSTEM_NAME_1);
        expect(expressionFactory.getCodesystemUrl(MDR_URN_1)).andReturn(CODESYSTEM_URL_1);

        replay(expressionFactory);

        String cqlQuery = CqlTestHelper.trim(codesytesmDefinitionsBuilder.createCodesystemDefinitionsStatement(queryDto));
        assertThat("Error creating codesystem definition  for one field with codesytem.", CqlTestHelper.trim(cqlQuery), is(EXPECTED_CODESYTEM_DEFINITION_1));
    }

    @Test
    void test_createCodesystemDefinitionsStatement_WithTwoDifferentCodesystems() {
        SimpleQueryDto queryDto = createDtoWithOneField(MDR_URN_1, MDR_URN_2);

        expect(expressionFactory.getCodesystemName(MDR_URN_1)).andReturn(CODESYSTEM_NAME_1);
        expect(expressionFactory.getCodesystemUrl(MDR_URN_1)).andReturn(CODESYSTEM_URL_1);

        expect(expressionFactory.getCodesystemName(MDR_URN_2)).andReturn(CODESYSTEM_NAME_2);
        expect(expressionFactory.getCodesystemUrl(MDR_URN_2)).andReturn(CODESYSTEM_URL_2);

        replay(expressionFactory);

        String cqlQuery = CqlTestHelper.trim(codesytesmDefinitionsBuilder.createCodesystemDefinitionsStatement(queryDto));
        assertThat("Wrong number of codesystem statement for SampleMaterialType.", StringUtils.countMatches(cqlQuery, EXPECTED_CODESYTEM_DEFINITION_1), is(1));
        assertThat("Wrong number of codesystem statement for StorageTemperature.", StringUtils.countMatches(cqlQuery, EXPECTED_CODESYTEM_DEFINITION_2), is(1));
    }

    @Test
    void test_createCodesystemDefinitionsStatement_WithTwoDifferentCodesystemsInTwoEntities() {
        SimpleQueryDto queryDto = createDtoWithTwoFields(MDR_URN_1, MDR_URN_2);

        expect(expressionFactory.getCodesystemName(MDR_URN_1)).andReturn(CODESYSTEM_NAME_1);
        expect(expressionFactory.getCodesystemUrl(MDR_URN_1)).andReturn(CODESYSTEM_URL_1);

        expect(expressionFactory.getCodesystemName(MDR_URN_2)).andReturn(CODESYSTEM_NAME_2);
        expect(expressionFactory.getCodesystemUrl(MDR_URN_2)).andReturn(CODESYSTEM_URL_2);

        replay(expressionFactory);

        String cqlQuery = CqlTestHelper.trim(codesytesmDefinitionsBuilder.createCodesystemDefinitionsStatement(queryDto));
        assertThat("Wrong number of codesystem statement for SampleMaterialType.", StringUtils.countMatches(cqlQuery, EXPECTED_CODESYTEM_DEFINITION_1), is(1));
        assertThat("Wrong number of codesystem statement for StorageTemperature.", StringUtils.countMatches(cqlQuery, EXPECTED_CODESYTEM_DEFINITION_2), is(1));
    }

    @Test
    void test_createCodesystemDefinitionsStatement_WithTheSameCodesystemsInTwoEntities() {
        SimpleQueryDto queryDto = createDtoWithTwoFields(MDR_URN_1, MDR_URN_2);

        expect(expressionFactory.getCodesystemName(MDR_URN_1)).andReturn(CODESYSTEM_NAME_1);
        expect(expressionFactory.getCodesystemUrl(MDR_URN_1)).andReturn(CODESYSTEM_URL_1);

        expect(expressionFactory.getCodesystemName(MDR_URN_2)).andReturn(CODESYSTEM_NAME_1);
        expect(expressionFactory.getCodesystemUrl(MDR_URN_2)).andReturn(CODESYSTEM_URL_1);

        replay(expressionFactory);

        String cqlQuery = CqlTestHelper.trim(codesytesmDefinitionsBuilder.createCodesystemDefinitionsStatement(queryDto));
        assertThat("Error creating codesystem definition for two fields in two entities with the same codesytem.", cqlQuery, is(EXPECTED_CODESYTEM_DEFINITION_1));
    }

    @Test
    void test_createCodesystemDefinitionsStatement_WithSameCodesystemTwice() {
        SimpleQueryDto queryDto = createDtoWithOneField(MDR_URN_1, MDR_URN_2);

        expect(expressionFactory.getCodesystemName(MDR_URN_1)).andReturn(CODESYSTEM_NAME_1);
        expect(expressionFactory.getCodesystemUrl(MDR_URN_1)).andReturn(CODESYSTEM_URL_1);

        expect(expressionFactory.getCodesystemName(MDR_URN_2)).andReturn(CODESYSTEM_NAME_1);
        expect(expressionFactory.getCodesystemUrl(MDR_URN_2)).andReturn(CODESYSTEM_URL_1);

        replay(expressionFactory);

        String cqlQuery = CqlTestHelper.trim(codesytesmDefinitionsBuilder.createCodesystemDefinitionsStatement(queryDto));
        assertThat("Error creating codesystem definition for two fields with the same codesytem.", cqlQuery, is(EXPECTED_CODESYTEM_DEFINITION_1));
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
