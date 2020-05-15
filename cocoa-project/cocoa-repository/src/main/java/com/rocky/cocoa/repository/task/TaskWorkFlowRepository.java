package com.rocky.cocoa.repository.task;

import com.rocky.cocoa.entity.task.TaskWorkFlow;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskWorkFlowRepository extends JpaRepository<TaskWorkFlow, String> {

  TaskWorkFlow findOneByName(String name);
}
