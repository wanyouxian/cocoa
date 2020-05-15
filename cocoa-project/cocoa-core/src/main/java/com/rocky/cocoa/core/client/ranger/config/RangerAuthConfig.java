package com.rocky.cocoa.core.client.ranger.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class RangerAuthConfig {

  private String username = "admin";
  private String password = "admin";

  @Override
  public String toString() {
    return "RangerAuthConfig{" +
        "username='" + username + '\'' +
        ", password='" + password + '\'' +
        '}';
  }
}