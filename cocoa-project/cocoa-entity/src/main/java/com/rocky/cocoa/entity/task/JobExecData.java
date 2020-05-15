package com.rocky.cocoa.entity.task;

import com.rocky.cocoa.entity.BaseEntity;
import com.rocky.cocoa.entity.JpaConverterJson;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Map;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "job_exec_data")
public class JobExecData extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String taskName;
  private String taskId;
  private String execId;
  private String jobId;
  private String jobName;
  @Column(columnDefinition = "TEXT")
  @Convert(converter = JpaConverterJson.class)
  private Map<String, Object> params;
  @Column(columnDefinition = "TEXT")
  @Convert(converter = JpaConverterJson.class)
  private Map<String, Object> outParams;

  public Object getParamValue(String name) {
    if (params != null && params.size() > 0) {
      return params.get(name);
    }
    return null;
  }

  public Object getOutParamValue(String name) {
    if (outParams != null && outParams.size() > 0) {
      return outParams.get(name);
    }
    return null;
  }
}
