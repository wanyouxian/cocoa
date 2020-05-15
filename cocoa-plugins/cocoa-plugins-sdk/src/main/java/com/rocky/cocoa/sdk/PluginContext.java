package com.rocky.cocoa.sdk;

import org.apache.hadoop.fs.FileSystem;

import java.io.IOException;

public interface PluginContext {

  public String getPrincipal();

  public PluginConfig getConfig();

  public String getHadoopHome();

  public FileSystem getFileSystem() throws IOException;

  //runtime info
  public String getTaskName();
  public String getFlowName();
  public String getRuntimeJobId();

  public String getJobName();

  public String getFlowExecId();

  public String getScheduleId();

  public void outputValues(PluginOutputValues outpuValues) throws IOException;

  public void saveTableInfo(String taskName, String dsName,
                            String dbName, String tableName,
                            String tableSchema) throws IOException;

  public void saveTableInfo(String dsName, String dbName,
                            String tableName, String tableSchema)
          throws IOException;

}
