package com.future.trader.common.enums.utils;

import com.sun.jna.FromNativeContext;
import com.sun.jna.ToNativeContext;
import com.sun.jna.TypeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class EnumConverter implements TypeConverter {

    private static final Logger logger = LoggerFactory.getLogger(EnumConverter.class);

    @Override
    public Object fromNative(Object nativeValue, FromNativeContext context) {
        Integer i = (Integer) nativeValue;
        Class targetClass = context.getTargetType();

        if (!JnaEnum.class.isAssignableFrom(targetClass)) {
            return null;
        }

        Object[] enums = targetClass.getEnumConstants();
        if (enums.length == 0) {
            logger.error("Could not convert desired enum type (), no valid values are defined.", targetClass.getName());
            return null;
        }

        JnaEnum instance = (JnaEnum) enums[0];
        return instance.getIntValue(i);
    }

    @Override
    public Object toNative(Object value, ToNativeContext context) {
        if (Objects.isNull(value)) {
            return new Integer(0);
        }

        JnaEnum jnaEnum = (JnaEnum) value;
        return new Integer(jnaEnum.getIntValue());

    }

    @Override
    public Class<?> nativeType() {
        return Integer.class;
    }
}
