package com.rocky.cocoa.server.query.dataframe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RowSchemaInfo {

  private List<ColumnInfo> columnInfos;
  private Map<String, Integer> nameIndexMap;

  public RowSchemaInfo(List<ColumnInfo> columnInfoList) {
    this.columnInfos = columnInfoList;
    nameIndexMap = new HashMap<>();
    int index = 0;
    for (ColumnInfo columnInfo : columnInfos) {
      nameIndexMap.put(columnInfo.getName(), new Integer(index));
      index++;
    }
  }

  public static RowSchemaInfoBuilder newSchemaBuilder() {
    return new RowSchemaInfoBuilder();
  }

  public int getColumnIndex(String name) {
    return nameIndexMap.get(name);
  }

  public String getColumnName(int index) {
    return columnInfos.get(index).getName();
  }

  public DataType getDataType(String name) {
    return columnInfos.get(this.getColumnIndex(name)).getType();
  }

  public DataType getDataType(int index) {
    return columnInfos.get(index).getType();
  }

  public List<ColumnInfo> getColumns() {
    return this.columnInfos;
  }

  public ColumnInfo getColumnInfo(int index) {
    return this.columnInfos.get(index);
  }

  public ColumnInfo getColumnInfo(String name) {
    return this.columnInfos.get(getColumnIndex(name));
  }

  public static class RowSchemaInfoBuilder {

    private List<ColumnInfo> columnInfos = new ArrayList<>();

    public RowSchemaInfoBuilder column(String name, DataType dataType) {
      columnInfos.add(new ColumnInfo(name, dataType));
      return this;
    }

    public RowSchemaInfo build() {
      return new RowSchemaInfo(columnInfos);
    }
  }
}
