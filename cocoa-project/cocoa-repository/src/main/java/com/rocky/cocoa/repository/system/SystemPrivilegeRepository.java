package com.rocky.cocoa.repository.system;

import com.rocky.cocoa.entity.system.PrivilegeType;
import com.rocky.cocoa.entity.system.SystemPrivilege;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemPrivilegeRepository extends JpaRepository<SystemPrivilege, Long> {
  SystemPrivilege findOneByTeamAndPrivilegeType(String team, PrivilegeType privilegeType);

}
