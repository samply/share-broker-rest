package de.samply.share.broker.utils.cql;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;

import de.samply.share.essentialquery.EssentialSimpleQueryDto;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

class EssentialSimpleQueryDto2CqlTransformerTest {

  private static final String LIBRARY_RETRIEVE = "library Retrieve";
  private static final String FHIR_VERSION_STATEMENT = "using FHIR version '4.0.0'";
  private static final String FHIRHELPERS_STATEMENT = "include FHIRHelpers version '4.0.0'";
  private static final String CODE_SYSTEM_STATEMENT = "codesystem SampleMaterialType: 'https://fhir.bbmri.de/CodeSystem/SampleMaterialType'";
  private static final String CONTEXT_STATEMENT = "context Specimen";
  private static final String INITIAL_POPULATION_STATEMENT = "define InInitialPopulation:";
  private static final String PREDICATE = "true";

  @SuppressWarnings("ConstantConditions")
  @Test
  void test_toQuery_emptyQuery() {
    EssentialSimpleQueryDto2CqlTransformer transformer = new EssentialSimpleQueryDto2CqlTransformer();

    String cql = transformer.toQuery(new EssentialSimpleQueryDto(), "Specimen");

    String trimmedCql = checkCqlPartAtStartPositionAndRemovePart(cql, LIBRARY_RETRIEVE,
        "Library should be 'Retrieve'");
    trimmedCql = checkCqlPartAtStartPositionAndRemovePart(trimmedCql, FHIR_VERSION_STATEMENT,
        "Wrong FHIR version statement");
    trimmedCql = checkCqlPartAtStartPositionAndRemovePart(trimmedCql, FHIRHELPERS_STATEMENT,
        "Wrong FHIRHelpers version");
    trimmedCql = checkCqlPartAtStartPositionAndRemovePart(trimmedCql, CODE_SYSTEM_STATEMENT,
        "Code System should be 'https://fhir.bbmri.de/CodeSystem/SampleMaterialType'");
    trimmedCql = checkCqlPartAtStartPositionAndRemovePart(trimmedCql, CONTEXT_STATEMENT,
        "Context should be 'Specimen'");
    trimmedCql = checkCqlPartAtStartPositionAndRemovePart(trimmedCql, INITIAL_POPULATION_STATEMENT,
        "Wrong define statement");
    checkCqlPartAtStartPositionAndRemovePart(trimmedCql, PREDICATE,
        "Predicate for an empty query should only be 'true'");
  }

  @Nullable
  private String checkCqlPartAtStartPositionAndRemovePart(String cql, String cqlStartPart,
      String message) {
    String trimmedCql = CqlTestHelper.trim(cql);

    assertThat(message, trimmedCql, startsWith(cqlStartPart));

    return StringUtils.removeStart(trimmedCql, cqlStartPart);
  }
}
