package com.rocky.cocoa.entity.task;

import com.rocky.cocoa.entity.BaseEntity;
import com.rocky.cocoa.entity.JpaConverterJson;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;
import java.util.Map;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "job_info")
public class JobInfo extends BaseEntity {

  @Id
  private String id;
  private String taskId;
  private String taskName;
  private Long pkgId;
  private String pkgName;
  private String pkgVersion;
  private String pkgPath;
  private String jobType;
  private String pkgLang;
  private String name;
  @Column(columnDefinition = "TEXT")
  @Convert(converter = JpaConverterJson.class)
  protected Map<String, Object> params;
  @Column(columnDefinition = "TEXT")
  @Convert(converter = JpaConverterJson.class)
  private Map<String, String> styleInfo;

  @Transient
  private List<JobInfo> depJobs;
}
