package de.samply.share.broker.utils.cql;

import de.samply.share.essentialquery.EssentialSimpleFieldDto;
import de.samply.share.essentialquery.EssentialSimpleQueryDto;
import de.samply.share.essentialquery.EssentialSimpleValueDto;
import de.samply.share.query.enums.SimpleValueCondition;
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

    static final String SINGLETON_NAME_1 = "Patient";
    static final String SINGLETON_NAME_2 = "Observation";

    @NotNull
    static EssentialSimpleQueryDto createSimpleQueryDto(EssentialSimpleFieldDto... fieldDtoList) {
        EssentialSimpleQueryDto queryDto = new EssentialSimpleQueryDto();
        for (EssentialSimpleFieldDto fieldDto : fieldDtoList) {
            queryDto.getFieldDtos().add(fieldDto);
        }

        return queryDto;
    }

    @NotNull
    static EssentialSimpleFieldDto createFieldDto(EssentialSimpleValueDto... valueDtoList) {
        return createFieldDto(MDR_URN_DEFAULT, valueDtoList);
    }

    @NotNull
    static EssentialSimpleFieldDto createFieldDto(String mdrUrn, EssentialSimpleValueDto... valueDtoList) {
        EssentialSimpleFieldDto fieldDto = new EssentialSimpleFieldDto();
        fieldDto.setUrn(mdrUrn);
        for (EssentialSimpleValueDto valueDto : valueDtoList) {
            fieldDto.getValueDtos().add(valueDto);
        }

        return fieldDto;
    }

    @NotNull
    static EssentialSimpleValueDto createValueDto(double value, double maxValue) {
        EssentialSimpleValueDto valueDto = new EssentialSimpleValueDto();
        valueDto.setCondition(SimpleValueCondition.EQUALS);
        valueDto.setValue(Double.toString(value));
        valueDto.setMaxValue(Double.toString(maxValue));

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
