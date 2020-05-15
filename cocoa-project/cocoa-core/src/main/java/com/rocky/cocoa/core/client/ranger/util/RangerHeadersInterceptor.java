package com.rocky.cocoa.core.client.ranger.util;

import feign.RequestInterceptor;
import feign.RequestTemplate;

public class RangerHeadersInterceptor implements RequestInterceptor {

  @Override
  public void apply(final RequestTemplate template) {
    template.header("Accept", "application/json");
    template.header("X-XSRF-HEADER", "\"\"");
    template.header("Content-Type", "application/json");
  }
}
