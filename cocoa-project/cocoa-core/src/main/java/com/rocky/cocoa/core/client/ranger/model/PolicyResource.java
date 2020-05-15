package com.rocky.cocoa.core.client.ranger.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PolicyResource {

  private List<String> values = Lists.newArrayList();
  private Boolean isExcludes;
  private Boolean isRecursive;

  @Override
  public String toString() {
    return "PolicyResource{" +
        "values=" + values +
        ", isExcludes=" + isExcludes +
        ", isRecursive=" + isRecursive +
        '}';
  }
}
