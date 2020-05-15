package com.rocky.cocoa.entity.task;

import com.rocky.cocoa.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "task_work_flow")
public class TaskWorkFlow extends BaseEntity {

  @Id
  private String id;
  private String name;
  private Integer azId;
  private String admin;
  private String team;
  @Column(columnDefinition = "INT")
  private Boolean scheduled;
  private String scheduleId;
  private String scheduleCron;
  private String description;
  private Integer executor;
  private String projectName;
  private Long projectId;

  @Transient
  private List<JobInfo> jobInfoList;

}
