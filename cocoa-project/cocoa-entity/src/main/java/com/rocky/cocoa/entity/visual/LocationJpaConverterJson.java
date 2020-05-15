package com.rocky.cocoa.entity.visual;

import cn.hutool.json.JSONUtil;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class LocationJpaConverterJson implements AttributeConverter<Location, String> {

  @Override
  public String convertToDatabaseColumn(Location stringObjectMap) {
    return JSONUtil.toJsonStr(stringObjectMap);
  }

  @Override
  public Location convertToEntityAttribute(String s) {
    return JSONUtil.toBean(s, Location.class);
  }
}
