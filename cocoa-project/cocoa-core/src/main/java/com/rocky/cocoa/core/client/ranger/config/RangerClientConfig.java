package com.rocky.cocoa.core.client.ranger.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import feign.Logger;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class RangerClientConfig {

  private int connectTimeoutMillis = 5 * 1000;
  private int readTimeoutMillis = 30 * 1000;
  private Logger.Level logLevel = Logger.Level.BASIC;

  private String url = "http://localhost:6080";

  private RangerAuthConfig authConfig = new RangerAuthConfig();

  @Override
  public String toString() {
    return "RangerClientConfig{" +
        "connectTimeoutMillis=" + connectTimeoutMillis +
        ", readTimeoutMillis=" + readTimeoutMillis +
        ", logLevel=" + logLevel +
        ", url='" + url + '\'' +
        ", authConfig=" + authConfig +
        '}';
  }

}
