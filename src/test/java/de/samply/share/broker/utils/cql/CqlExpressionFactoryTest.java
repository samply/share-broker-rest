package de.samply.share.broker.utils.cql;

import de.samply.share.query.enums.SimpleValueCondition;
import de.samply.share.query.value.ValueStringDto;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.CoreMatchers.is;

class CqlExpressionFactoryTest {

    private static final String URN_GENDER = "urn:mdr16:dataelement:23:1";
    private static final String URN_TEMPERATURE = "urn:mdr16:dataelement:17:1";
    private static final String URN_NOT_EXISTING = "urn:mdr16:dataelement:47:11";

    private static final String ENTITY_TYPE_PATIENT = "Patient";
    private static final String ENTITY_TYPE_SPECIMEN = "Specimen";
    private static final String ENTITY_TYPE_NOT_EXISTING = "Scientist";

    private static final String ECPECTED_PREAMBLE_TEMPLATE =
            "library Retrieve\n" +
                    "        using FHIR version '4.0.0'\n" +
                    "        include FHIRHelpers version '4.0.0'\n" +
                    "\n" +
                    "        library-infos\n" +
                    "\n" +
                    "        context Scientist\n" +
                    "\n" +
                    "        define InInitialPopulation:";

    private CqlExpressionFactory factory;

    @BeforeEach
    void initFactory() {
        URL url = CqlExpressionFactory.class.getResource("samply_cql_config.xml");
        File configFile = new File(url.getFile());

        factory = new CqlExpressionFactory(configFile);
    }

    @Test
    void test_getPreamble() {
        assertThat("Error reading preamble.", StringUtils.trim(factory.getPreamble(ENTITY_TYPE_NOT_EXISTING, "library-infos")), is(ECPECTED_PREAMBLE_TEMPLATE));
    }

    @Test
    void test_getPathExpression_existing_shortPath() {
        assertThat("Error reading path expression with path '{0}'.", StringUtils.trim(factory.getPathExpression(URN_GENDER, ENTITY_TYPE_PATIENT, "atomic-expression")), is("atomic-expression"));
    }

    @Test
    void test_getPathExpression_existing_longPath() {
        assertThat("Error reading path expression with path including text.", StringUtils.trim(factory.getPathExpression(URN_GENDER, ENTITY_TYPE_SPECIMEN, "atomic-expression")), is("exists(from [Patient] P where atomic-expression)"));
    }

    @Test
    void test_getPathExpression_notExistingEntityType() {
        assertThat("Error getting path expression for non-existing entity type.", StringUtils.trim(factory.getPathExpression(URN_GENDER, ENTITY_TYPE_NOT_EXISTING, "atomic-expression")), is(""));
    }

    @Test
    void test_getPathExpression_notExistingMdrUrn() {
        assertThat("Error getting path expression for non-existing MDR-urn.", StringUtils.trim(factory.getPathExpression(URN_NOT_EXISTING, ENTITY_TYPE_PATIENT, "atomic-expression")), is(""));
    }

    @Test
    void test_getAtomicExpression_operatorNotSpecifiedUseDefault() {
        ValueStringDto valueDto = createValueDto(SimpleValueCondition.EQUALS);

        CqlExpressionFactory.AtomicExpressionParameter atomicExpressionParameter = factory.createAtomicExpressionParameter(URN_GENDER, ENTITY_TYPE_SPECIMEN, valueDto);
        assertThat("Error reading atomic expression for unspecified operator using default expression.",
                StringUtils.trim(factory.getAtomicExpression(atomicExpressionParameter)),
                is("P.gender = '13'"));
    }

    @Test
    void test_getAtomicExpression_operatorSpecified() {
        ValueStringDto valueDto = createValueDto(SimpleValueCondition.BETWEEN);

        CqlExpressionFactory.AtomicExpressionParameter atomicExpressionParameter = factory.createAtomicExpressionParameter(URN_GENDER, ENTITY_TYPE_SPECIMEN, valueDto);
        assertThat("Error reading atomic expression for specified operator.",
                StringUtils.trim(factory.getAtomicExpression(atomicExpressionParameter)),
                is("(P.gender < '17' and P.gender > '13')"));
    }

    @Test
    void test_getAtomicExpression_notExistingMdrUrn() {
        ValueStringDto valueDto = createValueDto(SimpleValueCondition.BETWEEN);

        CqlExpressionFactory.AtomicExpressionParameter atomicExpressionParameter = factory.createAtomicExpressionParameter(URN_NOT_EXISTING, ENTITY_TYPE_SPECIMEN, valueDto);
        assertThat("Error getting atomic expression for non-existing MDR-urn.",
                StringUtils.trim(factory.getAtomicExpression(atomicExpressionParameter)),
                is(""));
    }

    @Test
    void test_getAtomicExpression_notExistingEntityType() {
        ValueStringDto valueDto = createValueDto(SimpleValueCondition.BETWEEN);

        CqlExpressionFactory.AtomicExpressionParameter atomicExpressionParameter = factory.createAtomicExpressionParameter(URN_GENDER, ENTITY_TYPE_NOT_EXISTING, valueDto);
        assertThat("Error getting atomic expression for non-existing entity type.",
                StringUtils.trim(factory.getAtomicExpression(atomicExpressionParameter)),
                is(""));
    }

    @Test
    void test_getExtensionUrl_blank() {
        assertThat("Error getting empty extension name.", StringUtils.trim(factory.getExtensionUrl(URN_GENDER)), is(""));
    }

    @Test
    void test_getExtensionUrl_filled() {
        assertThat("Error getting extension name.", StringUtils.trim(factory.getExtensionUrl(URN_TEMPERATURE)), is("https://fhir.bbmri.de/StructureDefinition/StorageTemperature"));
    }

    @Test
    void test_getCodesystemName_blank() {
        assertThat("Error getting empty name of code system.", StringUtils.trim(factory.getCodesystemName(URN_GENDER)), is(""));
    }

    @Test
    void test_getCodesystemName_filled() {
        assertThat("Error getting name of code system.", StringUtils.trim(factory.getCodesystemName(URN_TEMPERATURE)), is("StorageTemperature"));
    }

    @Test
    void test_getCodesystemUrl_blank() {
        assertThat("Error getting empty url of code system.", StringUtils.trim(factory.getCodesystemUrl(URN_GENDER)), is(""));
    }

    @Test
    void test_getCodesystemUrl_filled() {
        assertThat("Error getting url of code system.", StringUtils.trim(factory.getCodesystemUrl(URN_TEMPERATURE)), is("https://fhir.bbmri.de/CodeSystem/StorageTemperature"));
    }

    @Test
    void test_getCqlValue_configured() {
        assertThat("Error getting cql value for permitted value in config file.", StringUtils.trim(factory.getCqlValue(URN_TEMPERATURE, "RT")), is("temperatureRoom"));
    }

    @Test
    void test_getCqlValue_notConfigured() {
        final String not_configured = "NOT CONFIGURED";
        assertThat("Error getting cql value for permitted value not configured in config file.", StringUtils.trim(factory.getCqlValue(URN_TEMPERATURE, not_configured)), is(not_configured));
    }

    @NotNull
    private ValueStringDto createValueDto(SimpleValueCondition condition) {
        ValueStringDto valueDto = new ValueStringDto();
        valueDto.setCondition(condition);
        valueDto.setValue("13");
        valueDto.setMaxValue("17");

        return valueDto;
    }
}
