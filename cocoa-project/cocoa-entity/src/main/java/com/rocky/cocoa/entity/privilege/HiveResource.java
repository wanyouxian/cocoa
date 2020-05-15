package com.rocky.cocoa.entity.privilege;

import lombok.Data;

import java.util.List;

@Data public class HiveResource extends Resource {
  private String database;
  private String table;
  private String columns = "*";
  List<String> policys;
}
