package com.rocky.cocoa.server.query.dataframe;

import java.util.Date;
import java.util.function.Function;

public interface Column {

  public DataType getDataType();

  public <T> T apply(Function<Column, T> function);

  public int rowCount();

  public int getInt(int rowIndex);

  public double getDouble(int rowIndex);

  public boolean getBoolean(int rowIndex);

  public String getString(int rowIndex);

  public Date getDate(int rowIndex);

  public Object getValueAs(int rowIndex);
  public String getName();
  public Object[] getValues();
}
