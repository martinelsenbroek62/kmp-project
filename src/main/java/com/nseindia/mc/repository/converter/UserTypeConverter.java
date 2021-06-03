package com.nseindia.mc.repository.converter;

import com.nseindia.mc.model.UserType;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class UserTypeConverter implements AttributeConverter<UserType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(UserType type) {
        if (type == null) {
            return null;
        }
        return type.getCode();
    }

    @Override
    public UserType convertToEntityAttribute(Integer code) {
        return UserType.fromCode(code);
    }
}
