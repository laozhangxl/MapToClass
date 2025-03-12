package com.xxxx.plugin.domain.service.impl;

import java.beans.Transient;
import java.lang.reflect.Field;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapKeyConvert {

    public static List<Map<String, Object>> convertKeysToCamelCase(List<Map<String, Object>> list) {
        if (list == null) {
            return list;
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> map : list) {
            if (map != null) {
                Map<String, Object> convertedMap = new HashMap<>();
                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    String key = toCamelCase(entry.getKey().toLowerCase());
                    Object value = entry.getValue();
                    convertedMap.put(key, value);
                }
                result.add(convertedMap);
            }
        }
        return result;
    }


    private static String toCamelCase(String underscoreStr) {
        //USER_ID
        StringBuilder camelCaseStr = new StringBuilder();
        boolean nextUpperCase = false;
        for (char c : underscoreStr.toCharArray()) {
            if (c == '_') {
                nextUpperCase = true;
            } else {
                if (nextUpperCase) {
                    camelCaseStr.append(Character.toUpperCase(c));
                    nextUpperCase = false;
                } else {
                    camelCaseStr.append(c);
                }
            }
        }
        return camelCaseStr.toString();
    }

    /**
     * 根据泛型类型进行类型转换
     * @param keyType
     * @param valueType
     * @param targetType
     * @param objStr
     * @return
     */
    public static String toAnyType(String keyType, String valueType, String targetType, String objStr) {
        // 根据 valueType 和 targetType 进行类型转换
        if ("String".equals(valueType)) {
            if ("String".equals(targetType)) {
                return objStr;
            } else if ("Integer".equals(targetType) || "int".equals(targetType)) {
                return "Integer.valueOf(" + objStr + ")";
            } else if ("Long".equals(targetType) || "long".equals(targetType)) {
                return "Long.valueOf(" + objStr + ")";
            } else if ("Double".equals(targetType)) {
                return "Double.valueOf(" + objStr + ")";
            } else if ("Float".equals(targetType)) {
                return "Float.valueOf(" + objStr + ")";
            } else if ("Boolean".equals(targetType)) {
                return "Boolean.valueOf(" + objStr + ")";
            } else if ("Date".equals(targetType)) {
                return "new Date(" + objStr + ")";
            } else if ("LocalDate".equals(targetType)) {
                return "LocalDate.parse(" + objStr + ")";
            } else if ("BigDecimal".equals(targetType)) {
                return "new BigDecimal(" + objStr + ")";
            } else {
                // 默认处理为 String
                return objStr;
            }
        } else if ("Integer".equals(valueType) || "int".equals(valueType)) {
            if ("String".equals(targetType)) {
                return "String.valueOf(" + objStr + ")";
            } else if ("Integer".equals(targetType) || "int".equals(targetType)) {
                return objStr;
            } else if ("Long".equals(targetType) || "long".equals(targetType)) {
                return "Long.valueOf(" + objStr + ")";
            } else if ("Double".equals(targetType)) {
                return "Double.valueOf(" + objStr + ")";
            } else if ("Float".equals(targetType)) {
                return "Float.valueOf(" + objStr + ")";
            } else {
                // 默认处理为 Integer
                return objStr;
            }
        } else if ("Long".equals(valueType) || "long".equals(valueType)) {
            if ("String".equals(targetType)) {
                return "String.valueOf(" + objStr + ")";
            } else if ("Integer".equals(targetType) || "int".equals(targetType)) {
                return "Integer.valueOf(" + objStr + ")";
            } else if ("Long".equals(targetType) || "long".equals(targetType)) {
                return objStr;
            } else if ("Double".equals(targetType)) {
                return "Double.valueOf(" + objStr + ")";
            } else {
                // 默认处理为 Long
                return objStr;
            }
        } else if ("Double".equals(valueType)) {
            if ("String".equals(targetType)) {
                return "String.valueOf(" + objStr + ")";
            } else if ("Integer".equals(targetType) || "int".equals(targetType)) {
                return "Integer.valueOf(" + objStr + ")";
            } else if ("Long".equals(targetType) || "long".equals(targetType)) {
                return "Long.valueOf(" + objStr + ")";
            } else if ("Double".equals(targetType)) {
                return objStr;
            } else {
                // 默认处理为 Double
                return objStr;
            }
        } else if ("Object".equals(valueType)) {
            // Object 类型需要根据 targetType 进行强制转换
            if ("String".equals(targetType)) {
                return "String.valueOf(" + objStr + ")";
            } else if ("Integer".equals(targetType) || "int".equals(targetType)) {
                return "Integer.valueOf(" + objStr + ")";
            } else if ("Long".equals(targetType) || "long".equals(targetType)) {
                return "Long.valueOf(" + objStr + ")";
            } else if ("Double".equals(targetType)) {
                return "Double.valueOf(" + objStr + ")";
            } else if ("Float".equals(targetType)) {
                return "Float.valueOf(" + objStr + ")";
            } else if ("Boolean".equals(targetType)) {
                return "Boolean.valueOf(" + objStr + ")";
            } else if ("Date".equals(targetType)) {
                return "new Date(" + objStr + ")";
            } else if ("LocalDate".equals(targetType)) {
                return "LocalDate.parse(" + objStr + ")";
            } else if ("BigDecimal".equals(targetType)) {
                return "new BigDecimal(" + objStr + ")";
            } else {
                // 默认处理为 Object
                return objStr;
            }
        } else {
            // 默认处理为 String
            return objStr;
        }
    }
    
    
    
}
