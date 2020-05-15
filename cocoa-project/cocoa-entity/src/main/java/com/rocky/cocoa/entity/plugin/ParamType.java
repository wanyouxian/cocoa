package com.rocky.cocoa.entity.plugin;

import cn.hutool.json.JSONUtil;

import java.util.List;
import java.util.Map;

public enum ParamType {

  INT, FLOAT, STRING, BOOLEAN, ARRAY, MAP;

  /**
   * parse from string.
   *
   * @param str str
   * @param type type
   * @return value
   */
  public static Object parse(String str, ParamType type) {
    if (type.equals(STRING)) {
      return str;
    } else if (type.equals(INT)) {
      return Integer.parseInt(str);
    } else if (type.equals(FLOAT)) {
      return Float.parseFloat(str);
    } else if (type.equals(BOOLEAN)) {
      return Boolean.parseBoolean(str);
    } else if (ARRAY.equals(type)) {
      return JSONUtil.toBean(str, List.class);
    } else if (MAP.equals(type)) {
      return JSONUtil.toBean(str, Map.class);
    }
    return null;
  }

  /**
   * check param value isEmpty.
   *
   * @param type type
   * @param value value
   * @return isEmpty
   */
  public static boolean isEmpty(ParamType type, Object value) {
    switch (type) {
      case STRING:
        return value == null || value.toString().isEmpty();
      case INT:
        return value == null || ((Number) value).intValue() == 0;
      case MAP:
        return value == null || ((Map) value).isEmpty();
      case ARRAY:
        return value == null || ((List) value).size() == 0;
      case FLOAT:
        return value == null || ((Number) value).floatValue() == 0.0;
      case BOOLEAN:
        return false;
      default:
        return true;
    }
  }
}
