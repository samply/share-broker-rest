package de.samply.share.broker.utils.cql;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
class CqlConfig {

    private String template;

    @XmlElement(name = "uiField")
    private List<CqlMdrFieldEntry> mdrFieldEntryList = new ArrayList<>();

    String getTemplate() {
        return template;
    }

    void setTemplate(String template) {
        this.template = template;
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

        public Codesystem() {
        }

        public Codesystem(String name, String url) {
            this.name = name;
            this.url = url;
        }

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

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Codesystem)) {
                return false;
            }

            Codesystem compare = (Codesystem) obj;

            EqualsBuilder builder = new EqualsBuilder();
            builder.append(this.name, compare.name);
            builder.append(this.url, compare.url);
            return builder.build();
        }

        @Override
        public int hashCode() {
            HashCodeBuilder builder = new HashCodeBuilder();
            builder.append(name);
            builder.append(url);
            return builder.build();
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    static class Singleton {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Singleton)) {
                return false;
            }

            Singleton compare = (Singleton) obj;

            EqualsBuilder builder = new EqualsBuilder();
            builder.append(this.name, compare.name);
            return builder.build();
        }

        @Override
        public int hashCode() {
            HashCodeBuilder builder = new HashCodeBuilder();
            builder.append(name);
            return builder.build();
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

        @XmlElement(name = "singleton")
        private List<Singleton> singletonList = new ArrayList<>();

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

        public List<Singleton> getSingletonList() {
            return singletonList;
        }

        public void setSingletonList(List<Singleton> singletonList) {
            this.singletonList = singletonList;
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
