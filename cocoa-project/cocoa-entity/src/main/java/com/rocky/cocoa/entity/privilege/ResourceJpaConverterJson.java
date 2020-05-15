package com.rocky.cocoa.entity.privilege;

import cn.hutool.json.JSONUtil;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class ResourceJpaConverterJson implements AttributeConverter<Resource, String> {

  @Override
  public String convertToDatabaseColumn(Resource stringObjectMap) {
    return JSONUtil.toJsonStr(stringObjectMap);
  }

  @Override
  public Resource convertToEntityAttribute(String s) {
    return JSONUtil.toBean(s, Resource.class);
  }
}
