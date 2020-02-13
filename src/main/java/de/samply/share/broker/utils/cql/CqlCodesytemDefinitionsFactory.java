package de.samply.share.broker.utils.cql;

import de.samply.share.essentialquery.EssentialSimpleFieldDto;
import de.samply.share.essentialquery.EssentialSimpleQueryDto;
import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

class CqlCodesytemDefinitionsFactory {

    private final CqlExpressionFactory cqlExpressionFactory;

    CqlCodesytemDefinitionsFactory(CqlExpressionFactory cqlExpressionFactory) {
        this.cqlExpressionFactory = cqlExpressionFactory;
    }

    String create(EssentialSimpleQueryDto queryDto) {
        return createCodesystemDefinition(queryDto.getFieldDtos());
    }

    private String createCodesystemDefinition(List<EssentialSimpleFieldDto> fieldsDto) {
        Set<String> codesystemDefinitions = new HashSet<>();

        for (EssentialSimpleFieldDto fieldDto : fieldsDto) {
            String mdrUrn = fieldDto.getUrn();

            for (CqlConfig.Codesystem codesystem : cqlExpressionFactory.getCodesystems(mdrUrn)) {

                String codesystemName = codesystem.getName();
                String codesystemUrl = codesystem.getUrl();
                if (!StringUtils.isBlank(codesystemName) && !StringUtils.isBlank(codesystemUrl)) {
                    codesystemDefinitions.add(MessageFormat.format("codesystem {0}: ''{1}''",
                            codesystemName, codesystemUrl));
                }
            }
        }

        List<String> codesystemDefinitionsSorted = codesystemDefinitions.stream().sorted().collect(Collectors.toList());
        return StringUtils.join(codesystemDefinitionsSorted, "\n");
    }
}
