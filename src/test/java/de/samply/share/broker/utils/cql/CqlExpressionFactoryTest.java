package de.samply.share.broker.utils.cql;

import de.samply.share.query.enums.SimpleValueCondition;
import de.samply.share.query.value.ValueStringDto;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

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
        factory = new CqlExpressionFactory();
    }

    @Test
    void test_getPreamble() {
        String preamble = factory.getPreamble(ENTITY_TYPE_NOT_EXISTING, "library-infos");
        assertThat("Error reading preamble.", StringUtils.trim(preamble), is(ECPECTED_PREAMBLE_TEMPLATE));
    }

    @Test
    void test_getPathExpression_existing_shortPath() {
        String pathExpression = factory.getPathExpression(URN_GENDER, ENTITY_TYPE_PATIENT, "values-expression");
        assertThat("Error reading path expression with path '{0}'.", StringUtils.trim(pathExpression), is("values-expression"));
    }

    @Test
    void test_getPathExpression_existing_longPath() {
        String pathExpression = factory.getPathExpression(URN_GENDER, ENTITY_TYPE_SPECIMEN, "values-expression");
        assertThat("Error reading path expression with path including text.", StringUtils.trim(pathExpression), is("exists(from [Patient] P where values-expression)"));
    }

    @Test
    void test_getPathExpression_notExistingEntityType() {
        String pathExpression = factory.getPathExpression(URN_GENDER, ENTITY_TYPE_NOT_EXISTING, "values-expression");
        assertThat("Error getting path expression for non-existing entity type.", StringUtils.trim(pathExpression), is(""));
    }

    @Test
    void test_getPathExpression_notExistingMdrUrn() {
        String pathExpression = factory.getPathExpression(URN_NOT_EXISTING, ENTITY_TYPE_PATIENT, "values-expression");
        assertThat("Error getting path expression for non-existing MDR-urn.", StringUtils.trim(pathExpression), is(""));
    }

    @Test
    void test_getAtomicExpression_operatorNotSpecifiedUseDefault() {
        ValueStringDto valueDto = createValueDto(SimpleValueCondition.EQUALS);
        CqlExpressionFactory.AtomicExpressionParameter atomicExpressionParameter = factory.createAtomicExpressionParameter(URN_GENDER, ENTITY_TYPE_SPECIMEN, valueDto);

        String atomicExpression = factory.getAtomicExpression(atomicExpressionParameter);
        assertThat("Error reading atomic expression for unspecified operator using default expression.",
                StringUtils.trim(atomicExpression),
                is("P.gender = '13'"));
    }

    @Test
    void test_getAtomicExpression_operatorSpecified() {
        ValueStringDto valueDto = createValueDto(SimpleValueCondition.BETWEEN);
        CqlExpressionFactory.AtomicExpressionParameter atomicExpressionParameter = factory.createAtomicExpressionParameter(URN_GENDER, ENTITY_TYPE_SPECIMEN, valueDto);

        String atomicExpression = factory.getAtomicExpression(atomicExpressionParameter);
        assertThat("Error reading atomic expression for specified operator.",
                StringUtils.trim(atomicExpression),
                is("(P.gender < '17' and P.gender > '13')"));
    }

    @Test
    void test_getAtomicExpression_notExistingMdrUrn() {
        ValueStringDto valueDto = createValueDto(SimpleValueCondition.BETWEEN);
        CqlExpressionFactory.AtomicExpressionParameter atomicExpressionParameter = factory.createAtomicExpressionParameter(URN_NOT_EXISTING, ENTITY_TYPE_SPECIMEN, valueDto);

        String atomicExpression = factory.getAtomicExpression(atomicExpressionParameter);
        assertThat("Error getting atomic expression for non-existing MDR-urn.",
                StringUtils.trim(atomicExpression),
                is(""));
    }

    @Test
    void test_getAtomicExpression_notExistingEntityType() {
        ValueStringDto valueDto = createValueDto(SimpleValueCondition.BETWEEN);
        CqlExpressionFactory.AtomicExpressionParameter atomicExpressionParameter = factory.createAtomicExpressionParameter(URN_GENDER, ENTITY_TYPE_NOT_EXISTING, valueDto);

        String atomicExpression = factory.getAtomicExpression(atomicExpressionParameter);
        assertThat("Error getting atomic expression for non-existing entity type.",
                StringUtils.trim(atomicExpression),
                is(""));
    }

    @Test
    void test_getExtensionUrl_blank() {
        String extensionUrl = factory.getExtensionUrl(URN_GENDER);
        assertThat("Error getting empty extension name.", StringUtils.trim(extensionUrl), is(""));
    }

    @Test
    void test_getExtensionUrl_filled() {
        String extensionUrl = factory.getExtensionUrl(URN_TEMPERATURE);
        assertThat("Error getting extension name.", StringUtils.trim(extensionUrl), is("https://fhir.bbmri.de/StructureDefinition/StorageTemperature"));
    }

    @Test
    void test_getCodesystemName_blank() {
        String codesystemName = factory.getCodesystemName(URN_GENDER);
        assertThat("Error getting empty name of code system.", StringUtils.trim(codesystemName), is(""));
    }

    @Test
    void test_getCodesystemName_filled() {
        String codesystemName = factory.getCodesystemName(URN_TEMPERATURE);
        assertThat("Error getting name of code system.", StringUtils.trim(codesystemName), is("StorageTemperature"));
    }

    @Test
    void test_getCodesystemUrl_blank() {
        String codesystemUrl = factory.getCodesystemUrl(URN_GENDER);
        assertThat("Error getting empty url of code system.", StringUtils.trim(codesystemUrl), is(""));
    }

    @Test
    void test_getCodesystemUrl_filled() {
        String codesystemUrl = factory.getCodesystemUrl(URN_TEMPERATURE);
        assertThat("Error getting url of code system.", StringUtils.trim(codesystemUrl), is("https://fhir.bbmri.de/CodeSystem/StorageTemperature"));
    }

    @Test
    void test_getCqlValue_configured() {
        String cqlValue = factory.getCqlValue(URN_TEMPERATURE, "RT");
        assertThat("Error getting cql value for permitted value in config file.", StringUtils.trim(cqlValue), is("temperatureRoom"));
    }

    @Test
    void test_getCqlValue_notConfigured() {
        final String not_configured = "NOT CONFIGURED";
        String cqlValue = factory.getCqlValue(URN_TEMPERATURE, not_configured);
        assertThat("Error getting cql value for permitted value not configured in config file.", StringUtils.trim(cqlValue), is(not_configured));
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
