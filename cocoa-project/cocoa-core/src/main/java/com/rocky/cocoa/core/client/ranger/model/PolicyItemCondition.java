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
public class PolicyItemCondition {

  private String type;
  private List<String> values = Lists.newArrayList();

  @Override
  public String toString() {
    return "PolicyItemCondition{" +
        "type='" + type + '\'' +
        ", values=" + values +
        '}';
  }
}
