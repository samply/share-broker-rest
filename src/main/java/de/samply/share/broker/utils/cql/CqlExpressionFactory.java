package de.samply.share.broker.utils.cql;

import de.samply.share.query.enums.SimpleValueCondition;
import de.samply.share.query.value.AbstractQueryValueDto;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class CqlExpressionFactory {

    private final MultiKeyMap<String, CqlConfig.CqlAtomicExpressionEntry> mapAtomicExpressions = new MultiKeyMap<>();
    private final MultiKeyMap<String, CqlConfig.CqlEntityTypeEntry> mapPathExpressions = new MultiKeyMap<>();
    private final MultiKeyMap<String, String> mapPermittedValues = new MultiKeyMap<>();
    private final Map<String, List<CqlConfig.Codesystem>> mapCodesystems = new HashMap<>();
    private final Map<String, String> mapExtensions = new HashMap<>();

    private String preambleTemplate = "";

    private static final Logger logger = LogManager.getLogger(CqlExpressionFactory.class);

    CqlExpressionFactory() {
        try (InputStream cqlConfigStream = CqlExpressionFactory.class.getResourceAsStream("samply_cql_config.xml")) {
            initMaps(cqlConfigStream);
        } catch (IOException e) {
            logger.warn("No valid config resource 'samply_cql_config.xml' could be found", e);
        }
    }

    CqlExpressionFactory(InputStream cqlConfigStream) {
        initMaps(cqlConfigStream);
    }

    private void initMaps(InputStream cqlConfigStream) {
        CqlConfig mapping;
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(CqlConfig.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            mapping = (CqlConfig) unmarshaller.unmarshal(cqlConfigStream);
        } catch (JAXBException e) {
            logger.warn("Config resource 'samply_cql_config.xml' could not be unmarshalled: ", e);
            return;
        }

        initMaps(mapping);
    }

    private void initMaps(CqlConfig mapping) {
        this.preambleTemplate = mapping.getPreamble();

        for (CqlConfig.CqlMdrFieldEntry mdrFieldEntry : mapping.getMdrFieldEntryList()) {
            for (CqlConfig.Codesystem codesystem : mdrFieldEntry.getCodesystemList()) {
                List<CqlConfig.Codesystem> codesystems = mapCodesystems.getOrDefault(mdrFieldEntry.getMdrUrn(), new ArrayList<>());

                codesystems.add(codesystem);
                mapCodesystems.put(mdrFieldEntry.getMdrUrn(), codesystems);
            }

            if (!StringUtils.isBlank(mdrFieldEntry.getExtensionUrl())) {
                mapExtensions.put(mdrFieldEntry.getMdrUrn(), mdrFieldEntry.getExtensionUrl());
            }
        }

        for (CqlConfig.CqlMdrFieldEntry mdrFieldEntry : mapping.getMdrFieldEntryList()) {
            for (CqlConfig.CqlEntityTypeEntry entityTypeEntry : mdrFieldEntry.getEntityTypeEntryList()) {
                for (CqlConfig.CqlAtomicExpressionEntry atomicExpressionEntry : entityTypeEntry.getAtomicExpressionList()) {
                    mapAtomicExpressions.put(mdrFieldEntry.getMdrUrn(), entityTypeEntry.getEntityTypeName(), atomicExpressionEntry.getOperator(), atomicExpressionEntry);
                }
            }
        }

        for (CqlConfig.CqlMdrFieldEntry mdrFieldEntry : mapping.getMdrFieldEntryList()) {
            for (CqlConfig.CqlEntityTypeEntry entityTypeEntry : mdrFieldEntry.getEntityTypeEntryList()) {
                mapPathExpressions.put(mdrFieldEntry.getMdrUrn(), entityTypeEntry.getEntityTypeName(), entityTypeEntry);
            }
        }

        for (CqlConfig.CqlMdrFieldEntry mdrFieldEntry : mapping.getMdrFieldEntryList()) {
            for (CqlConfig.PermittedValueEntry permittedValueEntry : mdrFieldEntry.getPermittedValueEntryList()) {
                mapPermittedValues.put(mdrFieldEntry.getMdrUrn(), permittedValueEntry.getMdrKey(), permittedValueEntry.getCqlValue());
            }
        }
    }

    String getAtomicExpression(CqlExpressionFactory.AtomicExpressionParameter atomicExpressionParameter) {
        String mdrUrn = atomicExpressionParameter.getMdrUrn();
        String entityType = atomicExpressionParameter.getEntityType();
        String operator = atomicExpressionParameter.getOperator();

        CqlConfig.CqlAtomicExpressionEntry cqlAtomicExpressionEntry = mapAtomicExpressions.get(mdrUrn, entityType, operator);
        if (cqlAtomicExpressionEntry == null) {
            cqlAtomicExpressionEntry = mapAtomicExpressions.get(mdrUrn, entityType, "DEFAULT");
            if (cqlAtomicExpressionEntry == null) {
                logger.warn("No valid cql configuration found for entity type '" + entityType + "' and mdrUrn '" + mdrUrn + "' and operator '" + operator + "'");
                return "";
            }
        }

        //noinspection ConfusingArgumentToVarargsMethod
        return MessageFormat.format(cqlAtomicExpressionEntry.getAtomicCqlExpression(), atomicExpressionParameter.asVarArgParameter());
    }

    String getPathExpression(String mdrUrn, String entityType, String valuesExpression) {
        CqlConfig.CqlEntityTypeEntry cqlEntityTypeEntry1 = mapPathExpressions.get(mdrUrn, entityType);
        if (cqlEntityTypeEntry1 == null) {
            logger.warn("No valid cql configuration found for entity type '" + entityType + "' and mdrUrn '" + mdrUrn + "'");
            return "";
        }

        return MessageFormat.format(cqlEntityTypeEntry1.getPathCqlExpression(), valuesExpression);
    }

    String getPreamble(String entityType, String codesystems) {
        return MessageFormat.format(preambleTemplate, entityType, codesystems);
    }

    String getExtensionUrl(String mdrUrn) {
        return mapExtensions.getOrDefault(mdrUrn, "");
    }

    List<CqlConfig.Codesystem> getCodesystems(String mdrUrn) {
        return mapCodesystems.getOrDefault(mdrUrn, new ArrayList<>());
    }

    AtomicExpressionParameter createAtomicExpressionParameter(String mdrUrn, String entityType, AbstractQueryValueDto<?> valueDto) {
        return new AtomicExpressionParameter(mdrUrn, entityType, valueDto);
    }

    String getCqlValue(String mdrUrn, String mdrValue) {
        return StringUtils.defaultString(mapPermittedValues.get(mdrUrn, mdrValue), mdrValue);
    }

    class AtomicExpressionParameter {

        private final String mdrUrn;
        private final String entityType;
        private final SimpleValueCondition condition;
        private final String value;
        private final String maxValue;

        private final String extensionUrl;

        AtomicExpressionParameter(String mdrUrn, String entityType, AbstractQueryValueDto<?> valueDto) {
            this.mdrUrn = mdrUrn;
            this.entityType = entityType;
            this.condition = valueDto.getCondition();
            this.value = getCqlValue(mdrUrn, valueDto.getValueAsXmlString());
            this.maxValue = getCqlValue(mdrUrn, valueDto.getMaxValueAsXmlString());
            this.extensionUrl = getExtensionUrl(mdrUrn);
        }

        String getMdrUrn() {
            return mdrUrn;
        }

        String getEntityType() {
            return entityType;
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

        String[] asVarArgParameter() {
            return new String[]{getOperator(), extensionUrl, value, maxValue};
        }
    }
}
