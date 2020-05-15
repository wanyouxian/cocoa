package com.rocky.cocoa.server.query.dataframe;

public class ColumnInfo {

  private String name;
  private DataType type;

  public ColumnInfo(String name, DataType dataType) {
    this.name = name;
    this.type = dataType;
  }

  public ColumnInfo() {
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public DataType getType() {
    return type;
  }

  public void setType(DataType type) {
    this.type = type;
  }

  public boolean isNumerica() {
    switch (type) {
      case INT:
      case DOUBLE:
        return true;
      default:
        return false;
    }
  }
}
