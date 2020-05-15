package com.rocky.cocoa.entity.var;

import cn.hutool.json.JSONUtil;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class VarJpaConverterJson implements AttributeConverter<VariableValue, String> {

  @Override
  public String convertToDatabaseColumn(VariableValue stringObjectMap) {
    return JSONUtil.toJsonStr(stringObjectMap);
  }

  @Override
  public VariableValue convertToEntityAttribute(String s) {
    return JSONUtil.toBean(s,VariableValue.class);
  }
}
