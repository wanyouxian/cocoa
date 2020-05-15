package com.rocky.cocoa.server.var.model;

import com.rocky.cocoa.entity.var.VariableType;
import com.rocky.cocoa.entity.var.VariableValue;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public abstract class AbstractVariableValue implements VariableValue {

  protected VariableType variableType;
}
