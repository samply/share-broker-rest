package de.samply.share.broker.utils;

import de.samply.share.essentialquery.EssentialSimpleFieldDto;
import de.samply.share.essentialquery.EssentialSimpleQueryDto;
import de.samply.share.essentialquery.EssentialSimpleValueDto;
import de.samply.share.model.common.*;

import java.io.Serializable;
import java.util.List;

public class EssentialSimpleQueryDto2ShareXmlTransformer {

    public Query toQuery(EssentialSimpleQueryDto queryDto) {
        ObjectFactory objectFactory = new ObjectFactory();

        Query queryPojo = objectFactory.createQuery();
        Where wherePojo = objectFactory.createWhere();
        And andPojo = objectFactory.createAnd();

        addTermsToAndExpression(andPojo, queryDto.getFieldDtos());

        wherePojo.getAndOrEqOrLike().add(andPojo);
        queryPojo.setWhere(wherePojo);
        return queryPojo;
    }

    private void addTermsToAndExpression(And andPojo, List<EssentialSimpleFieldDto> fieldsDto) {
        for (EssentialSimpleFieldDto fieldDto : fieldsDto) {
            ObjectFactory objectFactory = new ObjectFactory();
            Or orPojo = objectFactory.createOr();

            addTermsToOrExpression(orPojo, fieldDto);

            andPojo.getAndOrEqOrLike().add(orPojo);
        }
    }

    private void addTermsToOrExpression(
            Or orPojo, EssentialSimpleFieldDto fieldDto) {
        for (EssentialSimpleValueDto valueDto : fieldDto.getValueDtos()) {
            Serializable eqPojo = getEgLtGtBetween(valueDto, fieldDto.getUrn());

            orPojo.getAndOrEqOrLike().add(eqPojo);
        }
    }

    private Serializable getEgLtGtBetween(EssentialSimpleValueDto valueDto, String urn) {
        ObjectFactory objectFactory = new ObjectFactory();

        Attribute valueAttribute = createAttribute(urn, valueDto.getValue());

        switch (valueDto.getCondition()) {
            case EQUALS:
                Eq eqPojo = objectFactory.createEq();
                eqPojo.setAttribute(valueAttribute);
                return eqPojo;
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

                String valueAsString = valueDto.getMaxValue();
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
