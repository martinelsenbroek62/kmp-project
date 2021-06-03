package com.nseindia.mc.repository.converter;

import com.nseindia.mc.model.PenaltyStatus;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class PenaltyStatusConverter implements AttributeConverter<PenaltyStatus, Integer> {

    @Override
    public Integer convertToDatabaseColumn(PenaltyStatus type) {
        if (type == null) {
            return null;
        }
        return type.getCode();
    }

    @Override
    public PenaltyStatus convertToEntityAttribute(Integer code) {
        return PenaltyStatus.fromCode(code);
    }
}
