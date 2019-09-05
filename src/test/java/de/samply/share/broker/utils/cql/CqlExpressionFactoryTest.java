package de.samply.share.broker.utils.cql;

import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;

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
        assertThat("Error reading atomic expression for unspecified operator using default expression.", StringUtils.trim(factory.getAtomicExpression(URN_GENDER, ENTITY_TYPE_SPECIMEN, "=", "13", "17")), is("P.gender = '13'"));
    }

    @Test
    void test_getAtomicExpression_operatorSpecified() {
        assertThat("Error reading atomic expression for specified operator.", StringUtils.trim(factory.getAtomicExpression(URN_GENDER, ENTITY_TYPE_SPECIMEN, "...", "13", "17")), is("(P.gender < '17' and P.gender > '13')"));
    }

    @Test
    void test_getAtomicExpression_notExistingMdrUrn() {
        assertThat("Error getting atomic expression for non-existing MDR-urn.", StringUtils.trim(factory.getAtomicExpression(URN_NOT_EXISTING, ENTITY_TYPE_SPECIMEN, "...", "13", "17")), is(""));
    }

    @Test
    void test_getAtomicExpression_notExistingEntityType() {
        assertThat("Error getting atomic expression for non-existing entity type.", StringUtils.trim(factory.getAtomicExpression(URN_GENDER, ENTITY_TYPE_NOT_EXISTING, "...", "13", "17")), is(""));
    }

    @Test
    void test_getExtensionName_blank() {
        assertThat("Error getting empty extension name.", StringUtils.trim(factory.getExtensionName(URN_GENDER)), is(""));
    }

    @Test
    void test_getExtensionName_filled() {
        assertThat("Error getting extension name.", StringUtils.trim(factory.getExtensionName(URN_TEMPERATURE)), is("https://fhir.bbmri.de/StructureDefinition/StorageTemperature"));
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

}
