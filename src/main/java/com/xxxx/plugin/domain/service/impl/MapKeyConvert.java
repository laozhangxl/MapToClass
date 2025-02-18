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
    
    public static String toAnyType(String param, String objStr, Map<String, String> paramTypeMap) {
        String typeStr = paramTypeMap.get(param);
        if (typeStr.equals("String")) {
            objStr = "String.valueOf(" + objStr + ")";
        } else if (typeStr.equals("Integer") || typeStr.equals("int")) {
            objStr = "(Integer) " + objStr;
        } else if (typeStr.equals("Long") || typeStr.equals("long")) {
            objStr = "(Long) " + objStr;
        } else if (typeStr.equals("Double")) {
            objStr = "(Double) " + objStr;
        } else if (typeStr.equals("Date")) {
            objStr = "(Date) " + objStr;
        } else if (typeStr.equals("LocalDate")) {
            objStr = "(LocalDate) " + objStr;
        } else if (typeStr.equals("BigDecimal")) {
            objStr = "new BigDecimal(String.valueOf(" + objStr + "))";
        }
        return objStr;
    }
    
    
    
}
