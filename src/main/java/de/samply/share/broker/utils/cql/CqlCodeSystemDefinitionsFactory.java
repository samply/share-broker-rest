package de.samply.share.broker.utils.cql;

import de.samply.share.essentialquery.EssentialSimpleFieldDto;
import de.samply.share.essentialquery.EssentialSimpleQueryDto;
import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

class CqlCodeSystemDefinitionsFactory {

    private final CqlExpressionFactory cqlExpressionFactory;

    CqlCodeSystemDefinitionsFactory(CqlExpressionFactory cqlExpressionFactory) {
        this.cqlExpressionFactory = cqlExpressionFactory;
    }

    String create(EssentialSimpleQueryDto queryDto, CqlConfig.Codesystem... fixCodeSystems) {
        return createCodeSystemDefinitions(queryDto.getFieldDtos(), fixCodeSystems);
    }

    private String createCodeSystemDefinitions(List<EssentialSimpleFieldDto> fieldsDto,
                                               CqlConfig.Codesystem... fixCodeSystems) {
        Set<String> codeSystemDefinitions = new HashSet<>();

        for (CqlConfig.Codesystem codeSystem : fixCodeSystems) {
            String definition = createCodeSystemDefinition(codeSystem);
            if (definition != null) {
                codeSystemDefinitions.add(definition);
            }
        }

        for (EssentialSimpleFieldDto fieldDto : fieldsDto) {
            String mdrUrn = fieldDto.getUrn();

            for (CqlConfig.Codesystem codeSystem : cqlExpressionFactory.getCodesystems(mdrUrn)) {
                String definition = createCodeSystemDefinition(codeSystem);
                if (definition != null) {
                    codeSystemDefinitions.add(definition);
                }
            }
        }

        return codeSystemDefinitions.stream().sorted().collect(Collectors.joining("\n"));
    }

    private static String createCodeSystemDefinition(CqlConfig.Codesystem codeSystem) {
        String name = codeSystem.getName();
        String url = codeSystem.getUrl();
        if (!StringUtils.isBlank(name) && !StringUtils.isBlank(url)) {
            return MessageFormat.format("codesystem {0}: ''{1}''", name, url);
        } else {
            return null;
        }
    }
}
