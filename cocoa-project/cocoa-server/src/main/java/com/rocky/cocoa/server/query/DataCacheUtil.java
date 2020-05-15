package com.rocky.cocoa.server.query;


import com.rocky.cocoa.server.query.dataframe.TableColumnSchema;

import java.lang.management.ManagementFactory;
import java.math.BigInteger;
import java.security.MessageDigest;

public class DataCacheUtil {

  public static final int bufferSize = 5242880;
  public static final int expireTime = 300;
  public static final int PageSize = 1000;
  public static final int MaxSize = 100000;

  public static String getUnionStr(String dbId, String sql) {
    return String.format("%s|%s", dbId, sql);
  }

  public static String getUnionStrWithPid(String dbId, String sql) {
    String pidWithHost = ManagementFactory.getRuntimeMXBean().getName();
    return String.format("%s|%s|%s", dbId, sql, pidWithHost);
  }

  public static String generateMd5(String str) {
    try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      md.update(str.getBytes());
      return new BigInteger(1, md.digest()).toString(16);
    } catch (Exception e) {
      throw new RuntimeException("generate md5 error", null);
    }
  }

  public static boolean checkSchema(TableColumnSchema schema) {
    switch (schema.getDataType()) {
      case BOOLEAN:
      case DOUBLE:
      case INT:
      case LONG:
        return false;
      case STRING:
      case TIME:
      case TIMESTAMP:
      case DATE:
        return true;
      default:
        return false;
    }
  }

  public static void closeClosables(AutoCloseable... closeables) {
    for (AutoCloseable closeable : closeables) {
      if (closeable != null) {
        try {
          closeable.close();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
  }
}
