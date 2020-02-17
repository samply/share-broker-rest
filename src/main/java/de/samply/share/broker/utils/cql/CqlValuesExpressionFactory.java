package de.samply.share.broker.utils.cql;

import de.samply.share.essentialquery.EssentialSimpleFieldDto;
import de.samply.share.essentialquery.EssentialSimpleValueDto;
import de.samply.share.essentialquery.EssentialValueType;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class CqlValuesExpressionFactory {

    private final CqlExpressionFactory cqlExpressionFactory;

    CqlValuesExpressionFactory(CqlExpressionFactory cqlExpressionFactory) {
        this.cqlExpressionFactory = cqlExpressionFactory;
    }

    String create(String mdrUrn, String entityType, EssentialSimpleFieldDto fieldDto) {
        List<String> atomicExpressions = new ArrayList<>();
        for (EssentialSimpleValueDto valueDto : fieldDto.getValueDtos()) {
            CollectionUtils.addAll(atomicExpressions, createSingleAtomicExpressionListForOneValueDto(mdrUrn, entityType, fieldDto.getValueType(), valueDto));
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

    private List<String> createSingleAtomicExpressionListForOneValueDto(String mdrUrn, String entityType, EssentialValueType valueType, EssentialSimpleValueDto valueDto) {
        List<CqlExpressionFactory.AtomicExpressionParameter> atomicExpressionParameterList = cqlExpressionFactory.createAtomicExpressionParameterList(mdrUrn, entityType, valueType, valueDto);
        if (CollectionUtils.isEmpty(atomicExpressionParameterList)) {
            return new ArrayList<>();
        }

        return atomicExpressionParameterList.stream()
                .map(cqlExpressionFactory::getAtomicExpression)
                .filter(cql -> !StringUtils.isBlank(cql))
                .collect(Collectors.toList());
    }
}
