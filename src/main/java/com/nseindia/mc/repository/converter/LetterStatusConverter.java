package com.nseindia.mc.repository.converter;

import com.nseindia.mc.model.PenaltyLetterStatus;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class LetterStatusConverter implements AttributeConverter<PenaltyLetterStatus, Integer> {

    @Override
    public Integer convertToDatabaseColumn(PenaltyLetterStatus type) {
        if (type == null) {
            return null;
        }
        return type.getCode();
    }

    @Override
    public PenaltyLetterStatus convertToEntityAttribute(Integer code) {
        return PenaltyLetterStatus.fromCode(code);
    }
}
