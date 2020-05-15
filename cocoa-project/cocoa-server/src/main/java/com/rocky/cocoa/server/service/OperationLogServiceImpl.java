package com.rocky.cocoa.server.service;

import com.google.common.base.Strings;
import com.rocky.cocoa.entity.system.OperationLog;
import com.rocky.cocoa.repository.system.OperationLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

@Service
public class OperationLogServiceImpl implements OperationLogService {
  @Resource
  OperationLogRepository operationLogRepository;

  @Override
  public void recordOperation(OperationLog operationLog) {
    operationLogRepository.save(operationLog);
  }

  @Override
  public Page<OperationLog> listOperationLogOrderByTime(int page, int size) {
    Sort sort = new Sort(Sort.Direction.DESC, "createTime");
    Pageable pageable =PageRequest.of(page,size,sort);
    return operationLogRepository.findAll(pageable);
  }

  @Override
  public Page<OperationLog> findOperationLogWithUserAndObjOrderByTime(String user, String obj, int page,
      int size) {
    return operationLogRepository.findAll((root, criteriaQuery, criteriaBuilder) -> {
      List<Predicate> ps = new ArrayList<>();
      if (!Strings.isNullOrEmpty(user)) {
        ps.add(criteriaBuilder.equal(root.get("user"), user));
      }
      if (!Strings.isNullOrEmpty(obj)) {
        ps.add(criteriaBuilder.like(root.get("obj"), "%" + obj + "%"));
      }
      criteriaQuery.where(ps.toArray(new Predicate[]{}));
      return null;
    }, PageRequest.of(page - 1, size));
  }

}
