package de.samply.share.broker.utils.cql;

import de.samply.share.query.entity.SimpleQueryDto;
import de.samply.share.query.field.AbstractQueryFieldDto;

import java.util.ArrayList;
import java.util.List;

class FieldDtoCollector {

    static List<AbstractQueryFieldDto<?, ?>> collect(SimpleQueryDto queryDto) {
        List<AbstractQueryFieldDto<?, ?>> fieldDtos = new ArrayList<>();

        fieldDtos.addAll(queryDto.getDonorDto().getFieldsDto());
        fieldDtos.addAll(queryDto.getSampleDto().getFieldsDto());

        return fieldDtos;
    }

}
