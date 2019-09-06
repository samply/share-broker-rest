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

        private String codesystemName;
        private String codesystemUrl;
        private String extensionUrl;

        @XmlElement(name = "entityType")
        private List<CqlEntityTypeEntry> entityTypeEntryList = new ArrayList<>();

        String getMdrUrn() {
            return mdrUrn;
        }

        void setMdrUrn(String mdrUrn) {
            this.mdrUrn = mdrUrn;
        }

        String getCodesystemName() {
            return codesystemName;
        }

        public void setCodesystemName(String codesystemName) {
            this.codesystemName = codesystemName;
        }

        String getCodesystemUrl() {
            return codesystemUrl;
        }

        public void setCodesystemUrl(String codesystemUrl) {
            this.codesystemUrl = codesystemUrl;
        }

        String getExtensionUrl() {
            return extensionUrl;
        }

        public void setExtensionUrl(String extensionUrl) {
            this.extensionUrl = extensionUrl;
        }

        List<CqlEntityTypeEntry> getEntityTypeEntryList() {
            return entityTypeEntryList;
        }

        void setEntityTypeEntryList(List<CqlEntityTypeEntry> entityTypeEntryList) {
            this.entityTypeEntryList = entityTypeEntryList;
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
