package com.rocky.cocoa.server.query.dataframe;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public interface DataFrame extends Iterable<Row> {

  public DataFrameMetaData getMetaData();

  public DataFrame slice(int startRow, int endRow);

  public int rowCount();

  public int columnCount();

  public DataFrame selectColumns(String[] columns);

  public DataFrame removeColumns(String[] columns);

  public List<Row> head(int count);

  public List<Row> tail(int count);

  public DataFrame unionAll(List<DataFrame> others);

  public void append(List<Object> rowValues);

  public void append(Object[] rowValues);

  public void serializeTo(OutputStream outputStream, DataFrameSerializer serializer);

  public Column getColumn(String name);

  public RowSchemaInfo getRowSchemaInfo();

  public Map<String, Object[]> transpose();

  public DataFrame aggregate(String[] aggrColumns, AggregateFuction[] fuctions,
                             String... groupColumns);

  public DataFrame distinct(String[] columns);


}
