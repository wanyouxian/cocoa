package com.rocky.cocoa.server.query.dataframe;

import lombok.Data;

@Data
public class TableColumnSchema {

  private String name;
  private String fieldType;
  private DataType dataType;
  private String comment;

}
