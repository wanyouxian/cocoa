package com.rocky.cocoa.repository.privilege;

import com.rocky.cocoa.entity.privilege.ResourcePrivilege;
import com.rocky.cocoa.entity.privilege.ResourceType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResourcePrivilegeRepository extends JpaRepository<ResourcePrivilege, Long> {
  ResourcePrivilege findOneByTeamAndResourceType(String team, ResourceType resourceType);

}
