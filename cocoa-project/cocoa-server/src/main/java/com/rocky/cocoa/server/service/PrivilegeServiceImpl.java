package com.rocky.cocoa.server.service;

import cn.hutool.core.util.ObjectUtil;
import com.rocky.cocoa.entity.privilege.ResourcePrivilege;
import com.rocky.cocoa.repository.privilege.ResourcePrivilegeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class PrivilegeServiceImpl implements PrivilegeService {

  @Resource
  ResourcePrivilegeRepository resourcePrivilegeRepository;
  @Autowired
  RangerService rangerService;

  @Override public void addPrivilege(ResourcePrivilege resourcePrivilege) {
    //todo add to ranger client
    rangerService.addPolicy(resourcePrivilege);
    resourcePrivilegeRepository.save(resourcePrivilege);
  }

  @Override public void updatePrivilege(ResourcePrivilege resourcePrivilege) {
    //todo 更新接口未实现
    rangerService.addPolicy(resourcePrivilege);
    resourcePrivilegeRepository.save(resourcePrivilege);
  }

  @Override public void delPrivilege(ResourcePrivilege resourcePrivilege) {
    //todo
    rangerService.removePolicy(resourcePrivilege.getName());
    resourcePrivilegeRepository.delete(resourcePrivilege);
  }

  @Override public void delPrivilege(long id) {
    //todo
    ResourcePrivilege one = resourcePrivilegeRepository.getOne(id);
    rangerService.removePolicy(one.getName());
    resourcePrivilegeRepository.delete(one);
  }

  @Override public ResourcePrivilege getPrivilege(long id) {
    return resourcePrivilegeRepository.getOne(id);
  }

  @Override public Page<ResourcePrivilege> listResourcePrivileges(String team, int page, int size, String sort,
                                                                  Direction direction) {
    ResourcePrivilege resourcePrivilege = new ResourcePrivilege();
    resourcePrivilege.setIsTrash(false);
    resourcePrivilege.setTeam(team);

    return resourcePrivilegeRepository.findAll(Example.of(resourcePrivilege), PageRequest.of(page, size,
        Sort.by(direction == null ? Direction.DESC : direction, ObjectUtil.isNull(sort) ? "id" : sort)));
  }
}
