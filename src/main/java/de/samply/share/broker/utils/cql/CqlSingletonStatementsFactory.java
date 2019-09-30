package de.samply.share.broker.utils.cql;

import de.samply.share.query.entity.SimpleQueryDto;
import de.samply.share.query.field.AbstractQueryFieldDto;
import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class CqlSingletonStatementsFactory {

    private final CqlExpressionFactory cqlExpressionFactory;

    CqlSingletonStatementsFactory(CqlExpressionFactory cqlExpressionFactory) {
        this.cqlExpressionFactory = cqlExpressionFactory;
    }

    String create(SimpleQueryDto queryDto, String entityType) {
        return createSingletonStatements(FieldDtoCollector.collect(queryDto), entityType);
    }

    private String createSingletonStatements(List<AbstractQueryFieldDto<?, ?>> fieldsDto, String entityType) {
        Set<String> singletonStatements = new HashSet<>();

        for (AbstractQueryFieldDto<?, ?> fieldDto : fieldsDto) {
            String mdrUrn = fieldDto.getUrn();

            for (CqlConfig.Singleton singleton : cqlExpressionFactory.getSingletons(mdrUrn, entityType)) {
                singletonStatements.add(MessageFormat.format("define {0}:\nsingleton from ([{0}])\n", singleton.getName()));
            }
        }

        return StringUtils.join(singletonStatements, "\n");
    }
}
