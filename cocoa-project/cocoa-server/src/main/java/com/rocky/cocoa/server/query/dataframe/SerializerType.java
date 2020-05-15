package com.rocky.cocoa.server.query.dataframe;

/**
 * Created by jixin.
 */
public enum SerializerType {
  JSON("json"),
  CSV("csv"),
  EXCEL("xls"),
  TEXT("txt");

  private String serializerName;

  SerializerType(String name) {
    this.serializerName = name;
  }

  public String getSerializerName() {
    return this.serializerName;
  }
}
