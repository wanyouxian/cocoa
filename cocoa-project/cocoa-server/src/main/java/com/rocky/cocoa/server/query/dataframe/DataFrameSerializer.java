package com.rocky.cocoa.server.query.dataframe;

import java.io.OutputStream;

public interface DataFrameSerializer {

  /**
   * Serialize dataframe to outputstream.
   *
   * @param outputStream ous
   * @param df df
   */
  public void serialize(OutputStream outputStream, DataFrame df);
}
