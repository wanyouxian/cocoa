package com.rocky.cocoa.server.var.model;


import com.rocky.cocoa.entity.plugin.ParamType;
import com.rocky.cocoa.entity.var.VariableType;

public class IndicatorValue extends AbstractVariableValue {

  private Object value;
  private ParamType paramType;

  public IndicatorValue() {
    this.variableType = VariableType.Indicator;
  }

  @Override
  public Object getValue() {
    return value;
  }

  @Override
  public void setValue(Object valueString) {
    this.value = valueString;
  }

  @Override
  public ParamType getParamType() {
    return paramType;
  }

  public void setParamType(ParamType paramType) {
    this.paramType = paramType;
  }

  @Override
  public void applyUpdate(Object value) {
    this.value = value;
  }

}
