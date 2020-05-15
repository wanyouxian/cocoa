package com.rocky.cocoa.core.client.ranger.api;

import com.rocky.cocoa.core.client.ranger.model.Policy;
import com.rocky.cocoa.core.client.ranger.util.RangerClientException;
import feign.Param;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@AllArgsConstructor
public class PolicyApis {

  private final PolicyFeignClient client;

  public Policy createPolicy(final Policy policy) throws RangerClientException {
    return client.createPolicy(policy);
  }

  public Policy getPolicyByName(@Param("serviceName") final String serviceName,
                                @Param("policyName") final String policyName) throws RangerClientException {
    return client.getPolicyByName(serviceName, policyName);
  }

  public List<Policy> getAllPoliciesByService(@Param("serviceName") final String serviceName)
      throws RangerClientException {
    return client.getAllPoliciesByService(serviceName);
  }

  public void deletePolicy(@Param("id") Integer id) {
    client.deletePolicy(id);
  }
}
