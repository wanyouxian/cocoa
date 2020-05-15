package com.rocky.cocoa.entity.task;

import com.rocky.cocoa.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "job_dep")
public class JobDep extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String taskId;
  private String taskName;
  private String jobId;
  private String jobName;
  private String jobDepId;
  private String jobDepName;

}
