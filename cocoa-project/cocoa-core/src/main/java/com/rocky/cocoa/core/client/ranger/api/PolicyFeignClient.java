package com.rocky.cocoa.core.client.ranger.api;

import com.rocky.cocoa.core.client.ranger.model.Policy;
import com.rocky.cocoa.core.client.ranger.util.RangerClientException;
import feign.Param;
import feign.RequestLine;

import java.util.List;

public interface PolicyFeignClient {

  /*
  Mapper to Ranger Policy APIs
   */
  @RequestLine("POST /service/public/v2/api/policy")
  Policy createPolicy(final Policy policy) throws RangerClientException;

  @RequestLine("GET /service/public/v2/api/service/{serviceName}/policy/{policyName}")
  Policy getPolicyByName(@Param("serviceName") final String serviceName,
                         @Param("policyName") final String policyName) throws RangerClientException;

  @RequestLine("GET /service/public/v2/api/service/{serviceName}/policy")
  List<Policy> getAllPoliciesByService(@Param("serviceName") final String serviceName)
      throws RangerClientException;

  @RequestLine("DELETE /service/plugins/policies/{id}")
  void deletePolicy(@Param("id") Integer id);
}
