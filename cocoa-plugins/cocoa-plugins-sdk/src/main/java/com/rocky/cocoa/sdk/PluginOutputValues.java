package com.rocky.cocoa.sdk;

import java.util.*;

/**
 * plugin output values.
 * only support int,float,boolean,string,list,map.
 * list,map support  primitive type only.
 */
public class PluginOutputValues {

  private static Set<String> supportClassName = new HashSet();

  static {
    supportClassName.add("int");
    supportClassName.add("java.lang.Integer");
    supportClassName.add("boolean");
    supportClassName.add("java.lang.Boolean");
    supportClassName.add("float");
    supportClassName.add("java.lang.Float");
    supportClassName.add("java.lang.Double");
    supportClassName.add("double");
    supportClassName.add("java.lang.String");
    supportClassName.add("java.util.Date");
  }

  private Map<String, Object> values = new HashMap<>();

  public void setValue(String key, Object value) {
    if (value != null) {
      checkType(value.getClass());
    }
    this.values.put(key, value);
  }

  public Object getValue(String key) {
    return values.get(key);
  }


  public Map<String, Object> getValues() {
    return values;
  }

  public void setList(String key, List<Object> list) {
    if (list.size() > 0) {
      Class clazz = list.get(0).getClass();
      checkType(clazz);
    }
    values.put(key, list);
  }


  public void setMap(String key, Map<String, Object> map) {
    if (map.size() > 0) {
      String vkey = map.keySet().iterator().next();
      Class clazz = map.get(vkey).getClass();
      checkType(clazz);
    }
    values.put(key, map);
  }

  private void checkType(Class clazz) {
    if (!clazz.isPrimitive() && !supportClassName.contains(clazz.getName())) {
      throw new RuntimeException("only primitive type and string support");
    }
  }

}
