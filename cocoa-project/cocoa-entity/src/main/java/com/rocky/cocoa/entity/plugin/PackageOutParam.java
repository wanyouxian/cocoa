package com.rocky.cocoa.entity.plugin;

import lombok.Data;

@Data
public class PackageOutParam {

  private String name;
  private ParamType type;
  private String description;
  private String defaultValue;
  private boolean required = false;
  private boolean userSetAble = false;
  private ParamEntityType entityType = ParamEntityType.PLAIN;
  private String example;

}
