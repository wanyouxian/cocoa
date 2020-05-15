package com.rocky.cocoa.server.var.model;


import com.rocky.cocoa.entity.plugin.ParamType;
import com.rocky.cocoa.entity.var.VariableType;

public class QueueValue extends AbstractVariableValue {

  private Object value;

  private ParamType paramType;

  public QueueValue() {
    this.variableType = VariableType.Queue;
  }

  public QueueValue(ParamType type) {
    this.paramType = type;
  }

  @Override
  public ParamType getParamType() {
    return this.paramType;
  }

  public void setParamType(ParamType paramType) {
    this.paramType = paramType;
  }

  @Override
  public void applyUpdate(Object value) {
    this.value = value;
  }


  @Override
  public Object getValue() {
    return value;
  }

  @Override
  public void setValue(Object valueString) {
    this.value = valueString;
  }
}
