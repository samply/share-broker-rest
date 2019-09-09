package de.samply.share.broker.utils.cql;

import de.samply.share.query.entity.SimpleQueryDto;
import de.samply.share.query.field.AbstractQueryFieldDto;
import de.samply.share.query.value.AbstractQueryValueDto;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SimpleQueryDto2CqlTransformer {

    private final CqlExpressionFactory cqlExpressionFactory;

    public SimpleQueryDto2CqlTransformer() {
        this(new CqlExpressionFactory());
    }

    SimpleQueryDto2CqlTransformer(CqlExpressionFactory cqlExpressionFactory) {
        this.cqlExpressionFactory = cqlExpressionFactory;
    }

    public String toQuery(SimpleQueryDto queryDto, String entityType) {
        String codesystemDefinitionsStatement = createCodesystemDefinitionsStatement(queryDto);
        String cqlPredicateStatement = createCqlPredicateStatment(queryDto, entityType);

        return cqlExpressionFactory.getPreamble(entityType, codesystemDefinitionsStatement) + cqlPredicateStatement;
    }

    private String createCodesystemDefinitionsStatement(SimpleQueryDto queryDto) {
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

    private String createCqlPredicateStatment(SimpleQueryDto queryDto, String entityType) {
        List<String> predicateList = new ArrayList<>();

        CollectionUtils.addIgnoreNull(predicateList, createCqlPredicate(entityType, queryDto.getDonorDto().getFieldsDto()));
        CollectionUtils.addIgnoreNull(predicateList, createCqlPredicate(entityType, queryDto.getSampleContextDto().getFieldsDto()));
        CollectionUtils.addIgnoreNull(predicateList, createCqlPredicate(entityType, queryDto.getSampleDto().getFieldsDto()));
        CollectionUtils.addIgnoreNull(predicateList, createCqlPredicate(entityType, queryDto.getEventDto().getFieldsDto()));

        if (predicateList.isEmpty()) {
            return "true";
        } else {
            return StringUtils.join(predicateList, " and ");
        }
    }

    private String createCqlPredicate(String entityType, List<AbstractQueryFieldDto<?, ?>> fieldsDto) {
        List<String> pathExpressionList = new ArrayList<>();

        for (AbstractQueryFieldDto<?, ?> fieldDto : fieldsDto) {
            String mdrUrn = fieldDto.getUrn();
            String atomicExpressions = createAtomicExpressionStatement(mdrUrn, entityType, fieldDto);

            if (!StringUtils.isEmpty(atomicExpressions)) {
                String pathExpression = cqlExpressionFactory.getPathExpression(mdrUrn, entityType, atomicExpressions);

                pathExpressionList.add(pathExpression);
            }
        }

        if (pathExpressionList.isEmpty()) {
            return null;
        } else if (pathExpressionList.size() == 1) {
            return pathExpressionList.get(0);
        } else {
            return StringUtils.join(pathExpressionList, " and ");
        }
    }

    private String createAtomicExpressionStatement(String mdrUrn, String entityType, AbstractQueryFieldDto<?, ?> fieldDto) {
        List<String> atomicExpressions = new ArrayList<>();
        for (AbstractQueryValueDto<?> valueDto : fieldDto.getValuesDto()) {
            CollectionUtils.addIgnoreNull(atomicExpressions, createSingleAtomicExpression(mdrUrn, entityType, valueDto));
        }

        if (atomicExpressions.isEmpty()) {
            return null;
        } else if (atomicExpressions.size() == 1) {
            return atomicExpressions.get(0);
        } else {
            return "(" +
                    StringUtils.join(atomicExpressions, " or ") +
                    ")";
        }
    }

    private String createSingleAtomicExpression(String mdrUrn, String entityType, AbstractQueryValueDto<?> valueDto) {
        CqlExpressionFactory.AtomicExpressionParameter atomicExpressionParameter = cqlExpressionFactory.createAtomicExpressionParameter(mdrUrn, entityType, valueDto);
        return cqlExpressionFactory.getAtomicExpression(atomicExpressionParameter);
    }
}
