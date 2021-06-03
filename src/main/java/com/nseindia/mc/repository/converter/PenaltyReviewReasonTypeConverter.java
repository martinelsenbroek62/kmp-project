package com.nseindia.mc.repository.converter;

import com.nseindia.mc.model.PenaltyReviewReasonType;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class PenaltyReviewReasonTypeConverter implements AttributeConverter<PenaltyReviewReasonType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(PenaltyReviewReasonType type) {
        if (type == null) {
            return null;
        }
        return type.getCode();
    }

    @Override
    public PenaltyReviewReasonType convertToEntityAttribute(Integer code) {
        return PenaltyReviewReasonType.fromCode(code);
    }
}
