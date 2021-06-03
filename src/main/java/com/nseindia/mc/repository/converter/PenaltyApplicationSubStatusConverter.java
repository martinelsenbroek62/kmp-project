package com.nseindia.mc.repository.converter;

import com.nseindia.mc.model.PenaltyApplicationSubStatus;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class PenaltyApplicationSubStatusConverter implements AttributeConverter<PenaltyApplicationSubStatus, Integer> {

    @Override
    public Integer convertToDatabaseColumn(PenaltyApplicationSubStatus type) {
        if (type == null) {
            return null;
        }
        return type.getCode();
    }

    @Override
    public PenaltyApplicationSubStatus convertToEntityAttribute(Integer code) {
        return PenaltyApplicationSubStatus.fromCode(code);
    }
}
