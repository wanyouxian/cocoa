package com.rocky.cocoa.server.query;


import com.rocky.cocoa.server.query.dataframe.DataFrame;

public interface ResultSetDataFrameWrapper<R> extends ResultSetWrapper<R, DataFrame> {

  public DataFrame wrapData(R result);
}
