package com.rocky.cocoa.server.var;


import com.rocky.cocoa.entity.var.ProjectVar;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import java.util.Map;

public interface VariableManager {

  void createVariable(ProjectVar projectVar);

  void update(ProjectVar var);


  void deleteVariable(long id);


  void bindVariable(String projectName, String varName, String taskName, String jobName, String jobParam);

  void unBindVariable(String projectName, String varName);

  boolean isBind(String projectName, String varName);

  ProjectVar getVariable(String projectName, String name);

  Page<ProjectVar> getVariables(String team, int pageIndex, int pageSize, String sort, Sort.Direction direction);

  public void applyUpdate(String taskName, String jobName, Map<String, Object> values);

}
