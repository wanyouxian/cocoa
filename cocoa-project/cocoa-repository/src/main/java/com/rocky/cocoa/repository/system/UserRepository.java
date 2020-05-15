package com.rocky.cocoa.repository.system;

import com.rocky.cocoa.entity.system.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long>{
  User findOneByName(String name);

}
