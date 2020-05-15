package com.rocky.cocoa.repository.meta;

import com.rocky.cocoa.entity.meta.ProjectInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectInfoRepository extends JpaRepository<ProjectInfo,Long> {
    ProjectInfo findByName(String name);
}
