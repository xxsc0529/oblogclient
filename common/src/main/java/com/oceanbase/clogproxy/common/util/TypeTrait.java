/* Copyright (c) 2021 OceanBase and/or its affiliates. All rights reserved.
oblogclient is licensed under Mulan PSL v2.
You can use this software according to the terms and conditions of the Mulan PSL v2.
You may obtain a copy of Mulan PSL v2 at:
         http://license.coscl.org.cn/MulanPSL2
THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
See the Mulan PSL v2 for more details. */

package com.oceanbase.clogproxy.common.util;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;

public class TypeTrait {
    public static boolean isNumber(Object obj) {
        return (obj instanceof Byte) || (obj instanceof Short) ||
                (obj instanceof Integer) || (obj instanceof Long);
    }

    public static boolean isNumber(Field field) {
        String typeName = field.getGenericType().getTypeName();
        return "byte".equals(typeName) || "java.lang.Byte".equals(typeName) ||
                "short".equals(typeName) || "java.lang.Short".equals(typeName) ||
                "int".equals(typeName) || "java.lang.Integer".equals(typeName) ||
                "long".equals(typeName) || "java.lang.Long".equals(typeName);
    }

    public static boolean isReal(Object obj) {
        return (obj instanceof Float) || (obj instanceof Double);
    }

    public static boolean isReal(Field field) {
        String typeName = field.getGenericType().getTypeName();
        return "float".equals(typeName) || "java.lang.Float".equals(typeName) ||
                "double".equals(typeName) || "java.lang.Double".equals(typeName);
    }

    public static boolean isBool(Object obj) {
        return obj instanceof Boolean;
    }

    public static boolean isBool(Field field) {
        String typeName = field.getGenericType().getTypeName();
        return "boolean".equals(typeName) || "java.lang.Boolean".equals(typeName);
    }

    public static boolean isString(Object obj) {
        return (obj instanceof Character) || (obj instanceof String);
    }

    public static boolean isString(Field field) {
        String typeName = field.getGenericType().getTypeName();
        return "char".equals(typeName) || "java.lang.Character".equals(typeName) ||
                "java.lang.String".equals(typeName);
    }

    public static boolean isSameLooseType(Object object, Field field) {
        return (isNumber(object) && isNumber(field)) ||
                (isReal(object) && isReal(field)) ||
                (isBool(object) && isBool(field)) ||
                (isString(object) && isString(field));
    }

    @SuppressWarnings("unchecked")
    public static <T> T fromString(String value, Class<?> clazz) {
        if (clazz == Byte.class || clazz == byte.class) {
            return (T) Byte.valueOf(value);
        } else if (clazz == Short.class || clazz == short.class) {
            return (T) Short.valueOf(value);
        } else if (clazz == Integer.class || clazz == int.class) {
            return (T) Integer.valueOf(value);
        } else if (clazz == Long.class || clazz == long.class) {
            return (T) Long.valueOf(value);
        } else if (clazz == Float.class || clazz == float.class) {
            return (T) Float.valueOf(value);
        } else if (clazz == Double.class || clazz == double.class) {
            return (T) Double.valueOf(value);
        } else if (clazz == Boolean.class || clazz == boolean.class) {
            return (T) (Boolean) (!StringUtils.isEmpty(value) && Boolean.parseBoolean(value));
        } else if (clazz == Character.class || clazz == char.class) {
            if (StringUtils.isNotEmpty(value)) {
                return (T) (Character) value.charAt(0);
            }
        } else if (clazz == String.class) {
            return (T) value;
        }
        return null;
    }
}
