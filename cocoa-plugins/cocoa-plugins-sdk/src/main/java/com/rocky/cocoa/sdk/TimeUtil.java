package com.rocky.cocoa.sdk;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtil {

  public static String format(Date date,String format) {
    SimpleDateFormat sdf = new SimpleDateFormat(format);
    return sdf.format(date);
  }
}

