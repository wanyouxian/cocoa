package com.rocky.cocoa.core.client.ranger.api;

import com.rocky.cocoa.core.client.ranger.model.User;
import com.rocky.cocoa.core.client.ranger.util.RangerClientException;
import feign.Param;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class UserApis {

  private final UserFeignClient client;

  public User createUser(final User user) throws RangerClientException {
    return client.createUser(user);
  }

  public void deleteUser(@Param("id") Integer id,@Param("forceDelete") boolean forceDelete) {
    client.deleteUser(id,forceDelete);
  }

  public User getUserByName(@Param("name") String name) throws RangerClientException {
    return client.getUserByName(name);
  }
}