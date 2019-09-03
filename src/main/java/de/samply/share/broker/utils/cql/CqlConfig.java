package de.samply.share.broker.utils.cql;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class CqlConfig {

    private String preamble;

    @XmlElement(name = "ui-field")
    private List<CqlMdrFieldEntry> mdrFieldEntryList = new ArrayList<>();

    public String getPreamble() {
        return preamble;
    }

    public void setPreamble(String preamble) {
        this.preamble = preamble;
    }

    public List<CqlMdrFieldEntry> getMdrFieldEntryList() {
        return mdrFieldEntryList;
    }

    public void setMdrFieldEntryList(List<CqlMdrFieldEntry> mdrFieldEntryList) {
        this.mdrFieldEntryList = mdrFieldEntryList;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class CqlMdrFieldEntry {
        private String mdrUrn;

        @XmlElement(name = "entity-type")
        private List<CqlEntityTypeEntry> entityTypeEntryList = new ArrayList<>();

        public String getMdrUrn() {
            return mdrUrn;
        }

        public void setMdrUrn(String mdrUrn) {
            this.mdrUrn = mdrUrn;
        }

        public List<CqlEntityTypeEntry> getEntityTypeEntryList() {
            return entityTypeEntryList;
        }

        public void setEntityTypeEntryList(List<CqlEntityTypeEntry> entityTypeEntryList) {
            this.entityTypeEntryList = entityTypeEntryList;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class CqlEntityTypeEntry {
        private String entityType;

        private String pathExpression;

        @XmlElement(name = "atomic-expression")
        private List<CqlAtomicExpressionEntry> atomicExpressionList = new ArrayList<>();

        public String getPathExpression() {
            return pathExpression;
        }

        public String getEntityType() {
            return entityType;
        }

        public void setEntityType(String entityType) {
            this.entityType = entityType;
        }

        public void setPathExpression(String pathExpression) {
            this.pathExpression = pathExpression;
        }

        public List<CqlAtomicExpressionEntry> getAtomicExpressionList() {
            return atomicExpressionList;
        }

        public void setAtomicExpressionList(List<CqlAtomicExpressionEntry> atomicExpressionList) {
            this.atomicExpressionList = atomicExpressionList;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class CqlAtomicExpressionEntry {
        private String operator;

        private String atomicExpression;

        public String getOperator() {
            return operator;
        }

        public void setOperator(String operator) {
            this.operator = operator;
        }

        public String getAtomicExpression() {
            return atomicExpression;
        }

        public void setAtomicExpression(String atomicExpression) {
            this.atomicExpression = atomicExpression;
        }
    }
}
