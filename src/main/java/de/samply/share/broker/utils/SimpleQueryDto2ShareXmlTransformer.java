package de.samply.share.broker.utils;

import de.samply.share.model.common.*;
import de.samply.share.query.entity.SimpleQueryDto;
import de.samply.share.query.field.AbstractQueryFieldDto;
import de.samply.share.query.value.AbstractQueryValueDto;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.List;

public class SimpleQueryDto2ShareXmlTransformer {

    public Query toQuery(SimpleQueryDto queryDto) {
        ObjectFactory objectFactory = new ObjectFactory();

        Query queryPojo = objectFactory.createQuery();
        Where wherePojo = objectFactory.createWhere();
        And andPojo = objectFactory.createAnd();

        addTermsToAndExpression(andPojo, queryDto.getDonorDto().getFieldsDto());
        addTermsToAndExpression(andPojo, queryDto.getSampleContextDto().getFieldsDto());
        addTermsToAndExpression(andPojo, queryDto.getSampleDto().getFieldsDto());
        addTermsToAndExpression(andPojo, queryDto.getEventDto().getFieldsDto());

        wherePojo.getAndOrEqOrLike().add(andPojo);
        queryPojo.setWhere(wherePojo);
        return queryPojo;
    }

    private void addTermsToAndExpression(And andPojo, List<AbstractQueryFieldDto<?, ?>> fieldsDto) {
        for (AbstractQueryFieldDto<?, ?> fieldDto : fieldsDto) {
            ObjectFactory objectFactory = new ObjectFactory();
            Or orPojo = objectFactory.createOr();

            addTermsToOrExpression(orPojo, fieldDto);

            andPojo.getAndOrEqOrLike().add(orPojo);
        }
    }

    private void addTermsToOrExpression(
            Or orPojo, AbstractQueryFieldDto<?, ?> fieldDto) {
        for (AbstractQueryValueDto<?> valueDto : fieldDto.getValuesDto()) {
            Serializable eqPojo = getEgLtGtBetween(valueDto, fieldDto.getUrn());

            orPojo.getAndOrEqOrLike().add(eqPojo);
        }
    }

    private Serializable getEgLtGtBetween(AbstractQueryValueDto<?> valueDto, String urn) {
        ObjectFactory objectFactory = new ObjectFactory();

        Attribute valueAttribute = createAttribute(urn, valueDto.getValueAsXmlString());

        switch (valueDto.getCondition()) {
            case EQUALS:
                Eq eqPojo = objectFactory.createEq();
                eqPojo.setAttribute(valueAttribute);
                return eqPojo;
            case LIKE:
                Like likePojo = objectFactory.createLike();
                likePojo.setAttribute(valueAttribute);
                return likePojo;
            case NOT_EQUALS:
                Neq neqPojo = objectFactory.createNeq();
                neqPojo.setAttribute(valueAttribute);
                return neqPojo;
            case LESS:
                Lt ltPojo = objectFactory.createLt();
                ltPojo.setAttribute(valueAttribute);
                return ltPojo;
            case LESS_OR_EQUALS:
                Leq leqPojo = objectFactory.createLeq();
                leqPojo.setAttribute(valueAttribute);
                return leqPojo;
            case GREATER:
                Gt gtPojo = objectFactory.createGt();
                gtPojo.setAttribute(valueAttribute);
                return gtPojo;
            case GREATER_OR_EQUALS:
                Geq geqPojo = objectFactory.createGeq();
                geqPojo.setAttribute(valueAttribute);
                return geqPojo;
            case BETWEEN:
                Geq minPojo = objectFactory.createGeq();
                minPojo.setAttribute(valueAttribute);

                String valueAsString = valueDto.getMaxValueAsXmlString();
                Attribute maxValueAttribute = createAttribute(urn, valueAsString);

                Leq maxPojo = objectFactory.createLeq();
                maxPojo.setAttribute(maxValueAttribute);

                And betweenPojo = new And();
                betweenPojo.getAndOrEqOrLike().add(minPojo);
                betweenPojo.getAndOrEqOrLike().add(maxPojo);

                return betweenPojo;
        }

        return null;
    }

    private Attribute createAttribute(String urn, String valueAsString) {
        ObjectFactory objectFactory = new ObjectFactory();

        Attribute maxValueAttribute = objectFactory.createAttribute();
        maxValueAttribute.setValue(objectFactory.createValue(valueAsString));
        maxValueAttribute.setMdrKey(urn);

        return maxValueAttribute;
    }
}
