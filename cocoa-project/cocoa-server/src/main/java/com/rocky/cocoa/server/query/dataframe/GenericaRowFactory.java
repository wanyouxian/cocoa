package com.rocky.cocoa.server.query.dataframe;

import java.util.ArrayList;
import java.util.List;

public class GenericaRowFactory {

  public static Row createRow(RowSchemaInfo schemaInfo, List<Object> values) {
    RowImpl row = new RowImpl(schemaInfo);
    for (int i = 0; i < row.getColumnCount(); i++) {
      row.setValue(i, values.get(i));
    }
    return row;
  }

  public static Row createRow(RowSchemaInfo schemaInfo, Object[] values) {
    RowImpl row = new RowImpl(schemaInfo);
    row.setRowValues(values);
    return row;
  }

  public static List<Row> createRows(RowSchemaInfo schemaInfo, List<List<Object>> valueList) {
    List<Row> rows = new ArrayList<>(valueList.size());
    for (List<Object> value : valueList) {
      rows.add(createRow(schemaInfo, value));
    }
    return rows;
  }

}
