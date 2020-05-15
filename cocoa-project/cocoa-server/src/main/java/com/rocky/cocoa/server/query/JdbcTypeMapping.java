package com.rocky.cocoa.server.query;


import com.rocky.cocoa.server.query.dataframe.DataType;

import java.sql.JDBCType;
import java.util.HashMap;
import java.util.Map;

public class JdbcTypeMapping {

  private static Map<JDBCType, DataType> jdbcTypeDataTypeMap;
  private static Map<DataType, ResultSetColumnReader> dataTypeReaderMap;

  static {
    jdbcTypeDataTypeMap = new HashMap<>();
    dataTypeReaderMap = new HashMap<>();
    jdbcTypeDataTypeMap.put(JDBCType.CHAR, DataType.STRING);
    jdbcTypeDataTypeMap.put(JDBCType.VARCHAR, DataType.STRING);
    jdbcTypeDataTypeMap.put(JDBCType.LONGNVARCHAR, DataType.STRING);
    jdbcTypeDataTypeMap.put(JDBCType.LONGVARCHAR, DataType.STRING);
    jdbcTypeDataTypeMap.put(JDBCType.BOOLEAN, DataType.BOOLEAN);
    jdbcTypeDataTypeMap.put(JDBCType.INTEGER, DataType.INT);
    jdbcTypeDataTypeMap.put(JDBCType.SMALLINT, DataType.INT);
    jdbcTypeDataTypeMap.put(JDBCType.TINYINT, DataType.INT);
    jdbcTypeDataTypeMap.put(JDBCType.BIT, DataType.INT);
    jdbcTypeDataTypeMap.put(JDBCType.DATE, DataType.DATE);
    jdbcTypeDataTypeMap.put(JDBCType.TIME, DataType.TIME);
    jdbcTypeDataTypeMap.put(JDBCType.TIMESTAMP, DataType.DATE);
    jdbcTypeDataTypeMap.put(JDBCType.DOUBLE, DataType.DOUBLE);
    jdbcTypeDataTypeMap.put(JDBCType.FLOAT, DataType.FLOAT);
    jdbcTypeDataTypeMap.put(JDBCType.BIGINT, DataType.LONG);
    jdbcTypeDataTypeMap.put(JDBCType.DECIMAL, DataType.DECIMAL);
    jdbcTypeDataTypeMap.put(JDBCType.REAL, DataType.FLOAT);

    dataTypeReaderMap.put(DataType.INT, new ResultSetColumnReader.IntReader());
    dataTypeReaderMap.put(DataType.DOUBLE, new ResultSetColumnReader.DoubleReader());
    dataTypeReaderMap.put(DataType.FLOAT, new ResultSetColumnReader.FloatReader());
    dataTypeReaderMap.put(DataType.STRING, new ResultSetColumnReader.StringReader());
    dataTypeReaderMap.put(DataType.DATE, new ResultSetColumnReader.DateReader());
    dataTypeReaderMap.put(DataType.BOOLEAN, new ResultSetColumnReader.BooleanReader());
    dataTypeReaderMap.put(DataType.LONG, new ResultSetColumnReader.BigIntReader());
    dataTypeReaderMap.put(DataType.DECIMAL, new ResultSetColumnReader.DecimalReader());

  }

  public static DataType getDataType(JDBCType jdbcType) {
    return jdbcTypeDataTypeMap.get(jdbcType);
  }

  public static ResultSetColumnReader getResultSetColumnReader(DataType dataType) {
    return dataTypeReaderMap.get(dataType);
  }


}
