package de.samply.share.broker.utils.cql;

import de.samply.share.query.entity.SimpleQueryDto;
import de.samply.share.query.field.AbstractQueryFieldDto;
import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

class CqlCodesytesmDefinitionsBuilder {

    private final CqlExpressionFactory cqlExpressionFactory;

    CqlCodesytesmDefinitionsBuilder(CqlExpressionFactory cqlExpressionFactory) {
        this.cqlExpressionFactory = cqlExpressionFactory;
    }

    String createCodesystemDefinitionsStatement(SimpleQueryDto queryDto) {
        List<AbstractQueryFieldDto<?,?>> combinedFieldDtoList = new ArrayList<>();

        combinedFieldDtoList.addAll(queryDto.getDonorDto().getFieldsDto());
        combinedFieldDtoList.addAll(queryDto.getSampleContextDto().getFieldsDto());
        combinedFieldDtoList.addAll(queryDto.getSampleDto().getFieldsDto());
        combinedFieldDtoList.addAll(queryDto.getEventDto().getFieldsDto());

        return createCodesystemDefinition(combinedFieldDtoList);
    }

    private String createCodesystemDefinition(List<AbstractQueryFieldDto<?, ?>> fieldsDto) {
        Set<String> codesystemDefinitions = new HashSet<>();

        for (AbstractQueryFieldDto<?, ?> fieldDto : fieldsDto) {
            String mdrUrn = fieldDto.getUrn();
            String codesystemUrl = cqlExpressionFactory.getCodesystemUrl(mdrUrn);
            String codesystemName = cqlExpressionFactory.getCodesystemName(mdrUrn);

            if (!StringUtils.isBlank(codesystemName) && !StringUtils.isBlank(codesystemUrl)) {
                codesystemDefinitions.add(MessageFormat.format("codesystem {0}: ''{1}''", codesystemName, codesystemUrl));
            }
        }

        List<String> codesystemDefinitionsSorted = codesystemDefinitions.stream().sorted().collect(Collectors.toList());
        return StringUtils.join(codesystemDefinitionsSorted, "\n");
    }
}
