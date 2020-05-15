package com.rocky.cocoa.server.query.dataframe;

import java.util.List;

public interface DataFrameMetaData {

  public int getColumnCount();

  public List<ColumnInfo> getColumns();

  public String getColumnName(int index);

  public int getColumnIndex(String name);

  public DataType getColumnType(int index);

  public DataType getColumnType(String name);


}
