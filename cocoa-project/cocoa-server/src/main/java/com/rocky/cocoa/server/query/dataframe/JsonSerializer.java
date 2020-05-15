package com.rocky.cocoa.server.query.dataframe;

import cn.hutool.json.JSONUtil;

import java.io.OutputStream;
import java.util.*;

public class JsonSerializer implements DataFrameSerializer {

  @Override
  public void serialize(OutputStream outputStream, DataFrame df) {
    if (outputStream != null) {
      Iterator<Row> rows = df.iterator();
      List<Map<String, Object>> result = new ArrayList<>();
      try {
        while (rows.hasNext()) {
          Row row = rows.next();
          Map<String, Object> map = new HashMap<>();
          for (ColumnInfo columnInfo : row.getColumnInfos()) {
            map.put(columnInfo.getName(), row.getValueAs(columnInfo.getName()));
          }
          result.add(map);
        }
        outputStream.write(JSONUtil.toJsonPrettyStr(result).getBytes());
      } catch (Exception e) {
        throw new RuntimeException("OutputStream write error", null);
      }
    } else {
      throw new RuntimeException("OutputStream is null", null);
    }
  }
}
