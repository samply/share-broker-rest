package de.samply.share.broker.utils.cql;

import de.samply.share.query.enums.SimpleValueCondition;
import de.samply.share.query.value.ValueStringDto;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class CqlExpressionFactoryTest {

    private static final String URN_GENDER = "urn:mdr16:dataelement:23:1";
    private static final String URN_TEMPERATURE = "urn:mdr16:dataelement:17:1";
    private static final String URN_NOT_EXISTING = "urn:mdr16:dataelement:47:11";
    private static final String URN_TWO_CODESYSTEMS = "urn:mdr16:dataelement:08:15";

    private static final String MDR_URN_PATIENT = "urn:mdr16:dataelement:08:16";
    private static final String MDR_URN_PATIENT_OBSERVATION = "urn:mdr16:dataelement:08:17";
    private static final String MDR_URN_PATIENT_PATIENT = "urn:mdr16:dataelement:08:18";

    private static final String ENTITY_TYPE_PATIENT = "Patient";
    private static final String ENTITY_TYPE_SPECIMEN = "Specimen";
    private static final String ENTITY_TYPE_NOT_EXISTING = "Scientist";

    private static final String ECPECTED_PREAMBLE_TEMPLATE =
            "library Retrieve\n" +
                    "using FHIR version '4.0.0'\n" +
                    "include FHIRHelpers version '4.0.0'\n" +
                    "\n" +
                    "codesystem-definitions\n" +
                    "\n" +
                    "context Scientist\n" +
                    "\n" +
                    "singleton-statements\n" +
                    "\n" +
                    "define InInitialPopulation:";

    private CqlExpressionFactory factory;

    @BeforeEach
    void initFactory() {
        factory = new CqlExpressionFactory();
    }

    @Test
    void test_getPreamble() {
        String preamble = factory.getPreamble(ENTITY_TYPE_NOT_EXISTING, "codesystem-definitions", "singleton-statements");
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
    void test_getCodesystems_empty() {
        Set<CqlConfig.Codesystem> codesystems = factory.getCodesystems(URN_GENDER);
        assertThat("Error getting empty list of codesystems.", codesystems, is(Collections.emptySet()));
    }

    @Test
    void test_getCodesystems_filled_name() {
        Set<CqlConfig.Codesystem> codesystems = factory.getCodesystems(URN_TEMPERATURE);
        assertThat("Error getting filled list of codesystems.", codesystems.size(), is(1));

        String codesystemName = codesystems.iterator().next().getName();
        assertThat("Error getting name of code system.", StringUtils.trim(codesystemName), is("StorageTemperature"));
    }

    @Test
    void test_getCodesystems_filled_url() {
        Set<CqlConfig.Codesystem> codesystems = factory.getCodesystems(URN_TEMPERATURE);
        assertThat("Error getting filled list of codesystems.", codesystems.size(), is(1));

        String codesystemUrl = codesystems.iterator().next().getUrl();
        assertThat("Error getting url of code system.", StringUtils.trim(codesystemUrl), is("https://fhir.bbmri.de/CodeSystem/StorageTemperature"));
    }

    @Test
    void test_getCodesystems_filled_twoCodesystems_names() {
        Set<CqlConfig.Codesystem> codesystems = factory.getCodesystems(URN_TWO_CODESYSTEMS);
        assertThat("Error getting filled list with 2 codesystems.", codesystems.size(), is(2));

        Set<String> expectedNames = new HashSet<>();
        expectedNames.add("loinc");
        expectedNames.add("loinc2");

        Set<String> actualNames = codesystems.stream().map(CqlConfig.Codesystem::getName).collect(Collectors.toSet());
        assertThat("Error getting names of list with 2 codesystems.", actualNames, is(expectedNames));
    }

    @Test
    void test_getCodesystems_filled_twoCodesystems_urls() {
        Set<CqlConfig.Codesystem> codesystems = factory.getCodesystems(URN_TWO_CODESYSTEMS);
        assertThat("Error getting filled list with 2 codesystems.", codesystems.size(), is(2));

        Set<String> expectedUrls = new HashSet<>();
        expectedUrls.add("http://loinc.org");
        expectedUrls.add("http://loinc2.org");

        Set<String> actualUrls = codesystems.stream().map(CqlConfig.Codesystem::getUrl).collect(Collectors.toSet());
        assertThat("Error getting names of list with 2 codesystems.", actualUrls, is(expectedUrls));
    }

    @Test
    void test_getSingletons_empty_forEntityType() {
        Set<CqlConfig.Singleton> singletons = factory.getSingletons(MDR_URN_PATIENT, ENTITY_TYPE_PATIENT);
        assertThat("Error getting empty list for entity type without singletons.", singletons, is(Collections.emptySet()));

    }

    @Test
    void test_getSingletons_filled_forEntityType() {
        Set<CqlConfig.Singleton> singletons = factory.getSingletons(MDR_URN_PATIENT, ENTITY_TYPE_SPECIMEN);
        assertThat("Error getting filled list with singleton.", singletons.size(), is(1));
    }

    @Test
    void test_getSingletons_filled_twoSingletons() {
        Set<CqlConfig.Singleton> singletons = factory.getSingletons(MDR_URN_PATIENT_OBSERVATION, ENTITY_TYPE_SPECIMEN);
        assertThat("Error getting filled list with 2 singletons.", singletons.size(), is(2));

        Set<String> expectedSingletons = new HashSet<>();
        expectedSingletons.add("Patient");
        expectedSingletons.add("Observation");

        Set<String> actualNames = singletons.stream().map(CqlConfig.Singleton::getName).collect(Collectors.toSet());
        assertThat("Error getting names of list with 2 singletons.", actualNames, is(expectedSingletons));
    }

    @Test
    void test_getSingletons_filled_twoIdenticalSingletons() {
        Set<CqlConfig.Singleton> singletons = factory.getSingletons(MDR_URN_PATIENT_PATIENT, ENTITY_TYPE_SPECIMEN);
        assertThat("Error getting filled list with 2 identical singletons.", singletons.size(), is(1));

        Set<String> expectedSingletons = new HashSet<>();
        expectedSingletons.add("Patient");

        Set<String> actualNames = singletons.stream().map(CqlConfig.Singleton::getName).collect(Collectors.toSet());
        assertThat("Error getting names of list with 2 singletons.", actualNames, is(expectedSingletons));
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
