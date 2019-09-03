package de.samply.share.broker.utils.cql;

import de.samply.config.util.FileFinderUtil;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileNotFoundException;
import java.text.MessageFormat;

public class CqlExpressionFactory {

    private final MultiKeyMap<String, CqlConfig.CqlAtomicExpressionEntry> mapAtomicExpressions = new MultiKeyMap<>();
    private final MultiKeyMap<String, CqlConfig.CqlEntityTypeEntry> mapPathExpressions = new MultiKeyMap<>();
    private String preambleTemplate = "";

    private static final Logger logger = LogManager.getLogger(CqlExpressionFactory.class);

    CqlExpressionFactory() {
        File cqlConfigFile;
        try {
            cqlConfigFile = FileFinderUtil.findFile("samply_cql_config.xml");
        } catch (FileNotFoundException e) {
            logger.warn("No valid config file 'samply_cql_config.xml' could be found", e);
            return;
        }

        CqlConfig mapping;
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(CqlConfig.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            mapping = (CqlConfig) unmarshaller.unmarshal(cqlConfigFile);
        } catch (JAXBException e) {
            logger.warn("Config file 'samply_cql_config.xml' could not be unmarshalled: ", e);
            return;
        }

        initMaps(mapping);
    }

    public CqlExpressionFactory(CqlConfig mapping) {
        initMaps(mapping);
    }

    private void initMaps(CqlConfig mapping) {
        this.preambleTemplate = mapping.getPreamble();

        for (CqlConfig.CqlMdrFieldEntry mdrFieldEntry : mapping.getMdrFieldEntryList()) {
            for (CqlConfig.CqlEntityTypeEntry entityTypeEntry : mdrFieldEntry.getEntityTypeEntryList()) {
                for (CqlConfig.CqlAtomicExpressionEntry atomicExpressionEntry : entityTypeEntry.getAtomicExpressionList()) {
                    mapAtomicExpressions.put(mdrFieldEntry.getMdrUrn(), entityTypeEntry.getEntityType(), atomicExpressionEntry.getOperator(), atomicExpressionEntry);
                }
            }
        }

        for (CqlConfig.CqlMdrFieldEntry mdrFieldEntry : mapping.getMdrFieldEntryList()) {
            for (CqlConfig.CqlEntityTypeEntry entityTypeEntry : mdrFieldEntry.getEntityTypeEntryList()) {
                mapPathExpressions.put(mdrFieldEntry.getMdrUrn(), entityTypeEntry.getEntityType(), entityTypeEntry);
            }
        }
    }

    String getAtomicExpression(String mdrUrn, String entityType, String operator, String... values) {
        CqlConfig.CqlAtomicExpressionEntry cqlAtomicExpressionEntry = mapAtomicExpressions.get(mdrUrn, entityType, operator);
        if (cqlAtomicExpressionEntry == null) {
            cqlAtomicExpressionEntry = mapAtomicExpressions.get(mdrUrn, entityType, "DEFAULT");
            if (cqlAtomicExpressionEntry == null) {
                logger.warn("No valid cql configuration found for entity type '" + entityType + "' and mdrUrn '" + mdrUrn + "' and operator '" + operator + "'");
                return "";
            }
        }

        Object[] operatorsAndValues = new Object[1 + values.length];
        operatorsAndValues[0] = operator;
        System.arraycopy(values, 0, operatorsAndValues, 1, values.length);

        return MessageFormat.format(cqlAtomicExpressionEntry.getAtomicExpression(), operatorsAndValues);
    }

    String getPathExpression(String mdrUrn, String entityType, String atomicExpressions) {
        CqlConfig.CqlEntityTypeEntry cqlEntityTypeEntry1 = mapPathExpressions.get(mdrUrn, entityType);
        if (cqlEntityTypeEntry1 == null) {
            logger.warn("No valid cql configuration found for entity type '" + entityType + "' and mdrUrn '" + mdrUrn + "'");
            return "";
        }

        return MessageFormat.format(cqlEntityTypeEntry1.getPathExpression(), atomicExpressions);
    }

    String getPreambleTemplate() {
        return preambleTemplate;
    }
}
