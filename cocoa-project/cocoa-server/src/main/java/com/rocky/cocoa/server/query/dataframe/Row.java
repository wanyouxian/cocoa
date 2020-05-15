package com.rocky.cocoa.server.query.dataframe;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public interface Row {

  void setValue(int index, Object value);
  void setValue(String name, Object value);
  int getInt(String name);
  int getInt(int index);
  double getDouble(String name);
  double getDouble(int index);
  boolean getBoolean(String name);
  boolean getBoolean(int index);
  Date getDate(String name);
  Date getDate(int index);
  String getString(String name);
  String getString(int index);
  Object getValueAs(String name);
  Object getValueAs(int index);
  int getColumnCount();
  List<ColumnInfo> getColumnInfos();

  ColumnInfo getColumnInfo(int index);

  <T> T apply(Function<Row, T> function);

  Object[] getRowValues();

  Object[] getRowValues(String[] names);

  void setRowValues(Object[] values);

  Map<String,Object> getRowMap();

}
