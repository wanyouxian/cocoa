package com.rocky.cocoa.core.client.ranger.api;

import com.rocky.cocoa.core.client.ranger.model.User;
import com.rocky.cocoa.core.client.ranger.util.RangerClientException;
import feign.Param;
import feign.RequestLine;

public interface UserFeignClient {

  /*
  Mapper to Ranger User APIs
   */
  @RequestLine("POST /service/xusers/secure/users")
  User createUser(final User user) throws RangerClientException;

  @RequestLine("DELETE /service/xusers/secure/users/id/{id}?forceDelete={forceDelete}")
  void deleteUser(@Param("id") Integer id, @Param("forceDelete") boolean forceDelete);

  @RequestLine("GET /service/xusers/users/userName/{name}")
  User getUserByName(@Param("name") String name) throws RangerClientException;

}
