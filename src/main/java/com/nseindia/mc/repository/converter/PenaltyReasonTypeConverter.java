package com.nseindia.mc.repository.converter;

import com.nseindia.mc.model.PenaltyReasonType;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class PenaltyReasonTypeConverter implements AttributeConverter<PenaltyReasonType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(PenaltyReasonType type) {
        if (type == null) {
            return null;
        }
        return type.getCode();
    }

    @Override
    public PenaltyReasonType convertToEntityAttribute(Integer code) {
        return PenaltyReasonType.fromCode(code);
    }
}
