package com.rocky.cocoa.server.service;

import com.rocky.cocoa.entity.privilege.ResourcePrivilege;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

public interface PrivilegeService {

  void addPrivilege(ResourcePrivilege resourcePrivilege);
  void updatePrivilege(ResourcePrivilege resourcePrivilege);

  void delPrivilege(ResourcePrivilege resourcePrivilege);

  void delPrivilege(long id);

  ResourcePrivilege getPrivilege(long id);

  Page<ResourcePrivilege> listResourcePrivileges(String team, int page, int size, String sort,
                                                 Sort.Direction direction);
}
