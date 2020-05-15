package com.rocky.cocoa.entity;

import cn.hutool.json.JSONUtil;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class JpaConverterJson implements AttributeConverter<Object, String> {

  @Override
  public String convertToDatabaseColumn(Object stringObjectMap) {
    return JSONUtil.toJsonStr(stringObjectMap);
  }

  @Override
  public Object convertToEntityAttribute(String s) {
    return JSONUtil.parse(s);
  }
}
