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

    @XmlElement(name = "ui-field")
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

        @XmlElement(name = "entity-type")
        private List<CqlEntityTypeEntry> entityTypeEntryList = new ArrayList<>();

        String getMdrUrn() {
            return mdrUrn;
        }

        void setMdrUrn(String mdrUrn) {
            this.mdrUrn = mdrUrn;
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
        private String entityType;

        private String pathExpression;

        @XmlElement(name = "atomic-expression")
        private List<CqlAtomicExpressionEntry> atomicExpressionList = new ArrayList<>();

        String getPathExpression() {
            return pathExpression;
        }

        String getEntityType() {
            return entityType;
        }

        void setEntityType(String entityType) {
            this.entityType = entityType;
        }

        void setPathExpression(String pathExpression) {
            this.pathExpression = pathExpression;
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

        private String atomicExpression;

        String getOperator() {
            return operator;
        }

        void setOperator(String operator) {
            this.operator = operator;
        }

        String getAtomicExpression() {
            return atomicExpression;
        }

        void setAtomicExpression(String atomicExpression) {
            this.atomicExpression = atomicExpression;
        }
    }
}
