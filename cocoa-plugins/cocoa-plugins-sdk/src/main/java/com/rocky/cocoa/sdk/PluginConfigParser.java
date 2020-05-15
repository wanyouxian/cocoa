package com.rocky.cocoa.sdk;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.BiConsumer;


public class PluginConfigParser {

  /**
   * parse plugin config.
   *
   * @param clazz config class
   * @param <T> config bean
   * @return T
   */
  public static <T> T parseConfig(Class<T> clazz, Properties properties) throws Exception {
    T t = clazz.newInstance();
    Field[] fields = clazz.getDeclaredFields();
    for (Field field : fields) {
      PluginParam pkgParam = field.getAnnotation(PluginParam.class);
      if (pkgParam != null) {
        String value = properties.getProperty(pkgParam.name());
        field.setAccessible(true);
        if (value != null && value.trim().length() > 0) {
          field.set(t, convertValue(field, value.trim(), pkgParam.timeFormat()));
        }
      }
    }
    return t;
  }

  /**
   * parse plugin config.
   *
   * @param clazz config class
   * @param <T> config bean
   * @return T
   */
  public static <T> T parseConfig(Class<T> clazz, Map<String, Object> properties) throws Exception {
    T t = clazz.newInstance();
    Field[] fields = clazz.getDeclaredFields();
    for (Field field : fields) {
      PluginParam pkgParam = field.getAnnotation(PluginParam.class);
      if (pkgParam != null) {
        Object valueObject = properties.get(pkgParam.name());
        if (valueObject == null) {
          continue;
        }
        field.setAccessible(true);
        if (valueObject != null) {
          Object obj = convertValue(field, valueObject, pkgParam.timeFormat());
          field.set(t, obj);
          Class filedClazz = field.getClass();
          if (Map.class.isAssignableFrom(filedClazz)
              || filedClazz.isArray()
              || List.class.isAssignableFrom(filedClazz)) {
            obj = JsonUtil.toJson(obj);
          } else if (filedClazz.equals(Date.class)) {
            obj = TimeUtil.format((Date) obj, pkgParam.timeFormat());
          }
          properties.put(pkgParam.name(), obj);
        }
      }
    }
    return t;
  }


  private static Object convertMap(Field field, Map srcValue, String timeFormat) {
    Map<String, Object> resultMap = new HashMap();
    final Type type = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[1];
    srcValue.forEach(new BiConsumer<String, Object>() {
      @Override
      public void accept(String s, Object o) {
        try {
          Object convertValue = convertValue(type, o, timeFormat);
          resultMap.put(s, convertValue);
        } catch (ParseException e) {
          e.printStackTrace();
        }
      }
    });
    return resultMap;
  }

  private static List buildList(Field field) {
    String name = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0]
        .getTypeName().toLowerCase();
    if (name.contains("string")) {

      return new ArrayList<String>();
    }
    if (name.contains("date")) {
      return new ArrayList<Date>();
    }

    if (name.contains("int")) {
      return new ArrayList<Integer>();
    }

    if (name.contains("float")) {
      return new ArrayList<Float>();
    }

    if (name.contains("double")) {
      return new ArrayList<Double>();
    }

    if (name.contains("boolean")) {
      return new ArrayList<Boolean>();
    }
    return null;
  }

  private static <T> T[] buildArray(Field field, int length) {
    String name = field.getType().getComponentType().getName().toLowerCase();
    if (name.contains("string")) {

      return (T[]) new String[length];
    }
    if (name.contains("date")) {
      return (T[]) new Date[length];
    }

    if (name.contains("int")) {
      return (T[]) new Integer[length];
    }

    if (name.contains("float")) {
      return (T[]) new Float[length];
    }

    if (name.contains("double")) {
      return (T[]) new Double[length];
    }

    if (name.contains("boolean")) {
      return (T[]) new Boolean[length];
    }
    return null;
  }


  private static Object convertList(Field field, List srcValue, String timeFormat)
      throws IOException, ParseException {
    Type clazz = null;
    if (field.getType().isArray()) {
      clazz = field.getType().getComponentType();
    } else {
      clazz = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
    }
    List distList = new ArrayList(srcValue.size());
    for (Object v : srcValue) {
      Object value = convertValue(clazz, v.toString(), timeFormat);
      distList.add(value);
    }
    if (field.getType().isArray()) {
      return distList.toArray(buildArray(field, distList.size()));
    } else {
      List list = buildList(field);
      list.addAll(distList);
      return list;
    }
  }

  private static Object convertValue(Type type, Object srcValue, String timeFormat)
      throws ParseException {
    String name = type.getTypeName().toLowerCase();
    String srcStringValue = srcValue.toString().trim();
    if (name.contains("string")) {
      return srcStringValue;
    }
    if (name.contains("date") && srcStringValue.length() > 0) {
      SimpleDateFormat sdf = new SimpleDateFormat(timeFormat);
      return sdf.parse(srcStringValue);
    }

    if (name.contains("int") && srcStringValue.length() > 0) {
      return Integer.valueOf(srcStringValue);
    }

    if (name.contains("float") && srcStringValue.length() > 0) {
      return Float.valueOf(srcStringValue);
    }

    if (name.contains("double") && srcStringValue.length() > 0) {
      return Double.valueOf(srcStringValue);
    }

    if (name.contains("boolean") && srcStringValue.length() > 0) {
      return Boolean.valueOf(srcStringValue);
    }

    return null;
  }

  private static Object convertValue(Field field, Object srcValue, String timeFormat)
      throws Exception {
    if (srcValue == null) {
      return null;
    }
    boolean isArray = field.getType().isArray()
        || List.class.isAssignableFrom(field.getType());
    if (isArray) {
      if (srcValue instanceof String) {
        if (srcValue.toString().length() == 0) {
          return Collections.emptyList();
        }
        srcValue = JsonUtil.fromJsonList(String.class, srcValue.toString());
      }
      return convertList(field, (List) srcValue, timeFormat);
    }

    boolean isMap = Map.class.isAssignableFrom(field.getType());
    if (isMap) {
      if (srcValue instanceof String) {
        if (srcValue.toString().length() == 0) {
          return Collections.emptyMap();
        }
        srcValue = JsonUtil.fromJsonMap(String.class, String.class, srcValue.toString());
      }
      return convertMap(field, (Map) srcValue, timeFormat);
    }
    return convertValue(field.getGenericType(), srcValue, timeFormat);
  }

}
