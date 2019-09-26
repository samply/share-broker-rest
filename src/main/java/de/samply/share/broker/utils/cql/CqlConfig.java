package de.samply.share.broker.utils.cql;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
class CqlConfig {

    private String preamble;

    @XmlElement(name = "uiField")
    private List<CqlMdrFieldEntry> mdrFieldEntryList = new ArrayList<>();

    String getPreamble() {
        return preamble;
    }

    void setPreamble(String preamble) {
        this.preamble = preamble;
    }

    List<CqlMdrFieldEntry> getMdrFieldEntryList() {
        return mdrFieldEntryList;
    }

    void setMdrFieldEntryList(List<CqlMdrFieldEntry> mdrFieldEntryList) {
        this.mdrFieldEntryList = mdrFieldEntryList;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    static class CqlMdrFieldEntry {
        private String mdrUrn;

        private String extensionUrl;

        @XmlElement(name = "codesystem")
        private List<Codesystem> codesystemList = new ArrayList<>();

        @XmlElement(name = "permittedValue")
        private List<PermittedValueEntry> permittedValueEntryList = new ArrayList<>();

        @XmlElement(name = "entityType")
        private List<CqlEntityTypeEntry> entityTypeEntryList = new ArrayList<>();

        String getMdrUrn() {
            return mdrUrn;
        }

        void setMdrUrn(String mdrUrn) {
            this.mdrUrn = mdrUrn;
        }

        String getExtensionUrl() {
            return extensionUrl;
        }

        public void setExtensionUrl(String extensionUrl) {
            this.extensionUrl = extensionUrl;
        }

        List<Codesystem> getCodesystemList() {
            return codesystemList;
        }

        public void setCodesystemList(List<Codesystem> codesystemList) {
            this.codesystemList = codesystemList;
        }

        List<PermittedValueEntry> getPermittedValueEntryList() {
            return permittedValueEntryList;
        }

        public void setPermittedValueEntryList(List<PermittedValueEntry> permittedValueEntryList) {
            this.permittedValueEntryList = permittedValueEntryList;
        }

        List<CqlEntityTypeEntry> getEntityTypeEntryList() {
            return entityTypeEntryList;
        }

        void setEntityTypeEntryList(List<CqlEntityTypeEntry> entityTypeEntryList) {
            this.entityTypeEntryList = entityTypeEntryList;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    static class Codesystem {
        private String name;
        private String url;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    static class PermittedValueEntry {
        private String mdrKey;
        private String cqlValue;

        String getMdrKey() {
            return mdrKey;
        }

        public void setMdrKey(String mdrKey) {
            this.mdrKey = mdrKey;
        }

        String getCqlValue() {
            return cqlValue;
        }

        public void setCqlValue(String cqlValue) {
            this.cqlValue = cqlValue;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    static class CqlEntityTypeEntry {
        private String entityTypeName;

        private String pathCqlExpression;

        @XmlElement(name = "atomicExpression")
        private List<CqlAtomicExpressionEntry> atomicExpressionList = new ArrayList<>();

        String getPathCqlExpression() {
            return pathCqlExpression;
        }

        String getEntityTypeName() {
            return entityTypeName;
        }

        void setEntityTypeName(String entityTypeName) {
            this.entityTypeName = entityTypeName;
        }

        void setPathCqlExpression(String pathCqlExpression) {
            this.pathCqlExpression = pathCqlExpression;
        }

        List<CqlAtomicExpressionEntry> getAtomicExpressionList() {
            return atomicExpressionList;
        }

        void setAtomicExpressionList(List<CqlAtomicExpressionEntry> atomicExpressionList) {
            this.atomicExpressionList = atomicExpressionList;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    static class CqlAtomicExpressionEntry {
        private String operator;

        private String atomicCqlExpression;

        String getOperator() {
            return operator;
        }

        void setOperator(String operator) {
            this.operator = operator;
        }

        String getAtomicCqlExpression() {
            return atomicCqlExpression;
        }

        void setAtomicCqlExpression(String atomicCqlExpression) {
            this.atomicCqlExpression = atomicCqlExpression;
        }
    }
}
