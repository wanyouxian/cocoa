package com.rocky.cocoa.core.client.ranger.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PolicyItemAccess {

  private String type;
  private Boolean isAllowed;

  @Override
  public String toString() {
    return "PolicyItemAccess{" +
        "type='" + type + '\'' +
        ", isAllowed=" + isAllowed +
        '}';
  }
}