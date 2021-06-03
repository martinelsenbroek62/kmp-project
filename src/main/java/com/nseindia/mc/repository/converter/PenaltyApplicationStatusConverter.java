package com.nseindia.mc.repository.converter;

import com.nseindia.mc.model.PenaltyApplicationStatus;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class PenaltyApplicationStatusConverter implements AttributeConverter<PenaltyApplicationStatus, Integer> {

    @Override
    public Integer convertToDatabaseColumn(PenaltyApplicationStatus type) {
        if (type == null) {
            return null;
        }
        return type.getCode();
    }

    @Override
    public PenaltyApplicationStatus convertToEntityAttribute(Integer code) {
        return PenaltyApplicationStatus.fromCode(code);
    }
}
