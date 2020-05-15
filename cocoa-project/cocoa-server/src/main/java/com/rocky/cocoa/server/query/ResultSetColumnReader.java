package com.rocky.cocoa.server.query;

import java.math.BigDecimal;
import java.sql.*;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public interface ResultSetColumnReader<T> {

  public T readValue(int index, ResultSet resultSet) throws SQLException;

  public static class IntReader implements ResultSetColumnReader<Integer> {

    @Override
    public Integer readValue(int index, ResultSet resultSet) throws SQLException {
      return new Integer(resultSet.getInt(index));
    }
  }

  public static class DoubleReader implements ResultSetColumnReader<Double> {

    @Override
    public Double readValue(int index, ResultSet resultSet) throws SQLException {
      return resultSet.getDouble(index);
    }
  }

  public static class FloatReader implements ResultSetColumnReader<Float> {

    @Override
    public Float readValue(int index, ResultSet resultSet) throws SQLException {
      return new Float(resultSet.getFloat(index));
    }
  }


  public static class StringReader implements ResultSetColumnReader<String> {

    @Override
    public String readValue(int index, ResultSet resultSet) throws SQLException {
      return resultSet.getString(index);
    }
  }

  public static class DateReader implements ResultSetColumnReader<Date> {

    @Override
    public Date readValue(int index, ResultSet resultSet) throws SQLException {
      if (resultSet.getObject(index) == null) {
        return null;
      }
      switch (resultSet.getMetaData().getColumnType(index)) {
        case Types.TIME:
        case Types.TIME_WITH_TIMEZONE:
          Time time = resultSet.getTime(index, Calendar.getInstance(TimeZone.getDefault()));
          return new Date(time.getTime());
        case Types.TIMESTAMP:
        case Types.TIMESTAMP_WITH_TIMEZONE:
          Timestamp timestamp = resultSet
              .getTimestamp(index, Calendar.getInstance(TimeZone.getDefault()));
          return new Date(timestamp.getTime());
        case Types.DATE:
          java.sql.Date date = resultSet
              .getDate(index, Calendar.getInstance(TimeZone.getDefault()));
          return new Date(date.getTime());
        default:
          return null;
      }
    }
  }

  public static class BooleanReader implements ResultSetColumnReader<Boolean> {

    @Override
    public Boolean readValue(int index, ResultSet resultSet) throws SQLException {
      return resultSet.getBoolean(index);
    }
  }

  public static class BigIntReader implements ResultSetColumnReader<Long> {

    @Override
    public Long readValue(int index, ResultSet resultSet) throws SQLException {
      return resultSet.getLong(index);
    }
  }

  public static class DecimalReader implements ResultSetColumnReader<String> {

    @Override
    public String readValue(int index, ResultSet resultSet) throws SQLException {
      BigDecimal decimal = resultSet.getBigDecimal(index);
      if (decimal == null) {
        return "NULL";
      }
      return decimal.toString();
    }
  }


}
