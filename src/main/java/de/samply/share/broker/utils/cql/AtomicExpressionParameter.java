package de.samply.share.broker.utils.cql;

import de.samply.share.query.enums.SimpleValueCondition;
import de.samply.share.query.value.AbstractQueryValueDto;

import java.util.ArrayList;
import java.util.List;

class AtomicExpressionParameter {

    private final String mdrUrn;
    private final String entityType;
    private final SimpleValueCondition condition;
    private final String value;
    private final String maxValue;

    private final String extensionUrl;
    private final String codesystemName;

    AtomicExpressionParameter(String mdrUrn, String entityType, AbstractQueryValueDto<?> valueDto, String extensionUrl, String codesystemName) {
        this.mdrUrn = mdrUrn;
        this.entityType = entityType;
        this.condition = valueDto.getCondition();
        this.value = valueDto.getValueAsXmlString();
        this.maxValue = valueDto.getMaxValueAsXmlString();
        this.extensionUrl = extensionUrl;
        this.codesystemName = codesystemName;
    }

    String getMdrUrn() {
        return mdrUrn;
    }

    String getEntityType() {
        return entityType;
    }

    String[] asVarArgParameter() {
        List<String> resultList = new ArrayList<>();

        resultList.add(getOperator());
        resultList.add(codesystemName);
        resultList.add(extensionUrl);
        resultList.add(value);

        if (condition == SimpleValueCondition.BETWEEN) {
            resultList.add(maxValue);
        }

        return resultList.toArray(new String[]{});
    }

    String getOperator() {
        switch (condition) {
            case BETWEEN:
                return "...";
            case EQUALS:
                return "=";
            case LIKE:
                return "~";
            case GREATER:
                return ">";
            case LESS:
                return "<";
            case NOT_EQUALS:
                return "<>";
            case LESS_OR_EQUALS:
                return "<=";
            case GREATER_OR_EQUALS:
                return ">=";
            default:
                return "default";
        }
    }
}
