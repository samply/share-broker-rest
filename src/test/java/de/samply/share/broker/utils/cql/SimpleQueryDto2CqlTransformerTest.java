package de.samply.share.broker.utils.cql;

import de.samply.share.query.common.MdrFieldDto;
import de.samply.share.query.entity.SimpleQueryDto;
import de.samply.share.query.enums.SimpleValueCondition;
import de.samply.share.query.field.FieldDecimalDto;
import de.samply.share.query.value.ValueDecimalDto;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class SimpleQueryDto2CqlTransformerTest {

    private static final String ENTITY_TYPE_PATIENT = "Patient";
    private static final String MDR_URN = "urn:mdr16:dataelement:23:1";

    private SimpleQueryDto2CqlTransformer transformer;

    @BeforeEach
    void initFactory() {
        URL url = CqlExpressionFactory.class.getResource("samply_cql_config.xml");
        File configFile = new File(url.getFile());

        CqlExpressionFactory factory = new CqlExpressionFactory(configFile);
        transformer = new SimpleQueryDto2CqlTransformer(factory);
    }

    @Test
    void test_toQuery_emptyDto() throws IOException {
        SimpleQueryDto queryDto = new SimpleQueryDto();
        String expected = readFile("SimpleQueryDto2CqlTransformerTest_emptyDto.txt");

        assertThat("Error creating query for empty DTO.", trim(transformer.toQuery(new SimpleQueryDto(), ENTITY_TYPE_PATIENT)), is(expected));
    }

    @Test
    void test_toQuery_oneFieldOneValue() throws IOException {
        SimpleQueryDto queryDto = createDtoWithOneFieldOneValue();
        String expected = readFile("SimpleQueryDto2CqlTransformerTest_oneFieldOneValue.txt");

        assertThat("Error creating query for one field and one value.", trim(transformer.toQuery(queryDto, ENTITY_TYPE_PATIENT)), is(expected));
    }

    @Test
    void test_toQuery_oneFieldOneValueBetweenOperator() throws IOException {
        SimpleQueryDto queryDto = createDtoWithOneFieldOneValueBetweenOperator();
        String expected = readFile("SimpleQueryDto2CqlTransformerTest_oneFieldOneValueBetweenOperator.txt");

        assertThat("Error creating query for one field and one value with special operator '...'.", trim(transformer.toQuery(queryDto, ENTITY_TYPE_PATIENT)), is(expected));
    }

    @Test
    void test_toQuery_twoFieldsOneValue() throws IOException {
        SimpleQueryDto queryDto = createDtoWithTwoFieldsOneValue();
        String expected = readFile("SimpleQueryDto2CqlTransformerTest_twoFieldsOneValue.txt");

        assertThat("Error creating query for two fields and one value each.", trim(transformer.toQuery(queryDto, ENTITY_TYPE_PATIENT)), is(expected));
    }

    @Test
    void test_toQuery_oneFieldTwoValues() throws IOException {
        SimpleQueryDto queryDto = createDtoWithOneFieldTwoValues();
        String expected = readFile("SimpleQueryDto2CqlTransformerTest_oneFieldTwoValues.txt");

        assertThat("Error creating query for one field and two values.", trim(transformer.toQuery(queryDto, ENTITY_TYPE_PATIENT)), is(expected));
    }

    @Test
    void test_toQuery_twoFieldsInTwoEntitiesOneValue() throws IOException {
        SimpleQueryDto queryDto = createDtoWithTwoFieldsInTwoEntitesOneValue();
        String expected = readFile("SimpleQueryDto2CqlTransformerTest_twoFieldsInTwoEntitiesOneValue.txt");

        assertThat("Error creating query for two fields in two entites and one value each.", trim(transformer.toQuery(queryDto, ENTITY_TYPE_PATIENT)), is(expected));
    }

    @Test
    void test_toQuery_twoFieldsTwoValues() throws IOException {
        SimpleQueryDto queryDto = createDtoWithTwoFieldsTwoValues();
        String expected = readFile("SimpleQueryDto2CqlTransformerTest_twoFieldsTwoValues.txt");

        assertThat("Error creating query for two fields and two values each.", trim(transformer.toQuery(queryDto, ENTITY_TYPE_PATIENT)), is(expected));
    }

    @NotNull
    private SimpleQueryDto createDtoWithOneFieldOneValue() {
        ValueDecimalDto valueDto = createValueDto(11.0, 47.0);
        FieldDecimalDto fieldDto = createFieldDto(valueDto);

        return createSimpleQueryDto(fieldDto);
    }

    @NotNull
    private SimpleQueryDto createDtoWithOneFieldOneValueBetweenOperator() {
        ValueDecimalDto valueDto = createValueDto(11.0, 47.0, SimpleValueCondition.BETWEEN);
        FieldDecimalDto fieldDto = createFieldDto(valueDto);

        return createSimpleQueryDto(fieldDto);
    }

    @NotNull
    private SimpleQueryDto createDtoWithOneFieldTwoValues() {
        ValueDecimalDto valueDto1 = createValueDto(11.0, 47.0);
        ValueDecimalDto valueDto2 = createValueDto(13.5, 29.5);

        FieldDecimalDto fieldDto = createFieldDto(valueDto1, valueDto2);

        return createSimpleQueryDto(fieldDto);
    }

    @NotNull
    private SimpleQueryDto createDtoWithTwoFieldsOneValue() {
        ValueDecimalDto valueDto1 = createValueDto(11.0, 47.0);
        FieldDecimalDto fieldDto1 = createFieldDto(valueDto1);

        ValueDecimalDto valueDto2 = createValueDto(13.5, 29.5);
        FieldDecimalDto fieldDto2 = createFieldDto(valueDto2);

        return createSimpleQueryDto(fieldDto1, fieldDto2);
    }

    @NotNull
    private SimpleQueryDto createDtoWithTwoFieldsInTwoEntitesOneValue() {
        ValueDecimalDto valueDto1 = createValueDto(11.0, 47.0);
        FieldDecimalDto fieldDto1 = createFieldDto(valueDto1);

        ValueDecimalDto valueDto2 = createValueDto(13.5, 29.5);
        FieldDecimalDto fieldDto2 = createFieldDto(valueDto2);

        return createSimpleQueryDtoWithTwoEntities(fieldDto1, fieldDto2);
    }

    @NotNull
    private SimpleQueryDto createDtoWithTwoFieldsTwoValues() {
        ValueDecimalDto valueDto1_1 = createValueDto(11.0, 47.0);
        ValueDecimalDto valueDto1_2 = createValueDto(13.5, 29.5);
        FieldDecimalDto fieldDto1 = createFieldDto(valueDto1_1, valueDto1_2);

        ValueDecimalDto valueDto2_1 = createValueDto(1.0, 7.0);
        ValueDecimalDto valueDto2_2 = createValueDto(3.5, 9.5);
        FieldDecimalDto fieldDto2 = createFieldDto(valueDto2_1, valueDto2_2);

        return createSimpleQueryDto(fieldDto1, fieldDto2);
    }

    @NotNull
    private SimpleQueryDto createSimpleQueryDto(FieldDecimalDto... fieldDtoList) {
        SimpleQueryDto queryDto = new SimpleQueryDto();
        for (FieldDecimalDto fieldDto : fieldDtoList) {
            queryDto.getDonorDto().getFieldsDto().add(fieldDto);
        }

        return queryDto;
    }

    @NotNull
    private SimpleQueryDto createSimpleQueryDtoWithTwoEntities(FieldDecimalDto fieldDtoDonor, FieldDecimalDto fieldDtoEvent) {
        SimpleQueryDto queryDto = new SimpleQueryDto();
        queryDto.getDonorDto().getFieldsDto().add(fieldDtoDonor);
        queryDto.getEventDto().getFieldsDto().add(fieldDtoEvent);

        return queryDto;
    }

    @NotNull
    private FieldDecimalDto createFieldDto(ValueDecimalDto... valueDtoList) {
        MdrFieldDto mdrFieldDto = creadeMdrFieldDto();

        FieldDecimalDto fieldDto = new FieldDecimalDto();
        fieldDto.setMdrFieldDto(mdrFieldDto);
        for (ValueDecimalDto valueDto : valueDtoList) {
            fieldDto.getValuesDto().add(valueDto);
        }

        return fieldDto;
    }

    @NotNull
    private MdrFieldDto creadeMdrFieldDto() {
        MdrFieldDto mdrFieldDto = new MdrFieldDto();
        mdrFieldDto.setUrn(MDR_URN);
        return mdrFieldDto;
    }

    @NotNull
    private ValueDecimalDto createValueDto(double value, double maxValue) {
        return createValueDto(value, maxValue, SimpleValueCondition.EQUALS);
    }

    @NotNull
    private ValueDecimalDto createValueDto(double value, double maxValue, SimpleValueCondition condition) {
        ValueDecimalDto valueDto = new ValueDecimalDto();
        valueDto.setCondition(condition);
        valueDto.setValue(value);
        valueDto.setMaxValue(maxValue);

        return valueDto;
    }

    private String readFile(String filename) throws IOException {
        URL url = CqlExpressionFactory.class.getResource(filename);
        File configFile = new File(url.getFile());

        String content = FileUtils.readFileToString(configFile, "UTF-8");
        return trim(content);
    }

    private String trim(String input) {
        return StringUtils.trim(input.replaceAll("\\s+", " "));
    }
}
