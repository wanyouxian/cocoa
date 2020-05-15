package com.rocky.cocoa.repository.meta;

import com.rocky.cocoa.entity.meta.DbInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DbInfoRepository extends JpaRepository<DbInfo,Long> {
    DbInfo findByName(String name);
    List<DbInfo> findByProjectName(String name);
    List<DbInfo> findByProjectId(Long id);
}
