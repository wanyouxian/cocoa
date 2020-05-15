package com.rocky.cocoa.server.query.dataframe;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.List;

public class CsvSerializer implements DataFrameSerializer {

  @Override
  public void serialize(OutputStream outputStream, DataFrame df) {
    if (outputStream != null) {
      BufferedWriter writer = null;
      try {
        writer = new BufferedWriter(new OutputStreamWriter(outputStream), 8192);
        List<ColumnInfo> list = df.getMetaData().getColumns();
        for (int i = 0; i < list.size(); i++) {
          ColumnInfo columnInfo = list.get(i);
          writer.write(columnInfo.getName());
          if (i != list.size() - 1) {
            writer.write(",");
          }
        }
        writer.newLine();
        Iterator<Row> rows = df.iterator();
        while (rows.hasNext()) {
          Row row = rows.next();
          writer.write(row.toString());
          if (rows.hasNext()) {
            writer.newLine();
          }
        }
        writer.flush();
      } catch (Exception e) {
        throw new RuntimeException("OutputStream write error");
      } finally {
        try {
          if (writer != null) {
            writer.close();
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    } else {
      throw new RuntimeException("OutputStream is null");
    }
  }
}
