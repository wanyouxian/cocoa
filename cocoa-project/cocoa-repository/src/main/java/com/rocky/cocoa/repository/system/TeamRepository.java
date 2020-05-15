package com.rocky.cocoa.repository.system;

import com.rocky.cocoa.entity.system.Team;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Long> {
 Team findOneByName(String name);
}
