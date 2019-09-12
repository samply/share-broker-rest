package de.samply.share.broker.utils.cql;

import de.samply.share.query.common.MdrFieldDto;
import de.samply.share.query.entity.SimpleQueryDto;
import de.samply.share.query.enums.SimpleValueCondition;
import de.samply.share.query.field.FieldDecimalDto;
import de.samply.share.query.value.ValueDecimalDto;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

class CqlTestHelper {

    static final String MDR_URN_1 = "urn:mdr16:dataelement:16:1";
    static final String MDR_URN_2 = "urn:mdr16:dataelement:17:1";
    private static final String MDR_URN_DEFAULT = "urn:mdr16:dataelement:0:0";

    static final String CODESYSTEM_NAME_1 = "StorageTemperatureType";
    static final String CODESYSTEM_NAME_2 = "SampleMaterialType";

    static final String CODESYSTEM_URL_1 = "https://fhir.bbmri.de/CodeSystem/StorageTemperatureType";
    static final String CODESYSTEM_URL_2 = "https://fhir.bbmri.de/CodeSystem/SampleMaterialType";

    static final String ENTITY_TYPE = "Patient";

    @NotNull
    static SimpleQueryDto createSimpleQueryDto(FieldDecimalDto... fieldDtoList) {
        SimpleQueryDto queryDto = new SimpleQueryDto();
        for (FieldDecimalDto fieldDto : fieldDtoList) {
            queryDto.getDonorDto().getFieldsDto().add(fieldDto);
        }

        return queryDto;
    }

    @NotNull
    static SimpleQueryDto createSimpleQueryDtoTwoFieldsInTwoEntities(FieldDecimalDto fieldDto1, FieldDecimalDto fieldDto2) {
        SimpleQueryDto queryDto = new SimpleQueryDto();

        queryDto.getDonorDto().getFieldsDto().add(fieldDto1);
        queryDto.getSampleDto().getFieldsDto().add(fieldDto2);

        return queryDto;
    }

    @NotNull
    static FieldDecimalDto createFieldDto(ValueDecimalDto... valueDtoList) {
        return createFieldDto(MDR_URN_DEFAULT, valueDtoList);
    }

    @NotNull
    static FieldDecimalDto createFieldDto(String mdrUrn, ValueDecimalDto... valueDtoList) {
        MdrFieldDto mdrFieldDto = creadeMdrFieldDto(mdrUrn);

        FieldDecimalDto fieldDto = new FieldDecimalDto();
        fieldDto.setMdrFieldDto(mdrFieldDto);
        for (ValueDecimalDto valueDto : valueDtoList) {
            fieldDto.getValuesDto().add(valueDto);
        }

        return fieldDto;
    }

    @NotNull
    private static MdrFieldDto creadeMdrFieldDto(String mdrUrn) {
        MdrFieldDto mdrFieldDto = new MdrFieldDto();
        mdrFieldDto.setUrn(mdrUrn);
        return mdrFieldDto;
    }

    @NotNull
    static ValueDecimalDto createValueDto(double value, double maxValue) {
        ValueDecimalDto valueDto = new ValueDecimalDto();
        valueDto.setCondition(SimpleValueCondition.EQUALS);
        valueDto.setValue(value);
        valueDto.setMaxValue(maxValue);

        return valueDto;
    }
    
    static String trim(String input) {
        String resultWithNormalizedBlankSpace = input
                .replaceAll("\\s+", " ")
                .replace("( ", "(")
                .replace(") ", ")")
                .replace(" (", "(")
                .replace(" )", ")");

        return StringUtils.trim(resultWithNormalizedBlankSpace);
    }

}
