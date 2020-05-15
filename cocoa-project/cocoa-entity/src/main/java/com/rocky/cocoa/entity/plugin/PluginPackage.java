package com.rocky.cocoa.entity.plugin;

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
@Table(name = "plugin_package")
public class PluginPackage extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String name;
  private String version;
  private String pluginType;
  @Enumerated(EnumType.STRING)
  private PluginCategory pluginCategory;
  private String jobType;
  private String lang;
  private String admin;
  private String team;
  private String projectName;
  private Long projectId;
  private String pluginDesc;
  @Column(columnDefinition = "TEXT")
  @Convert(converter = ParamJpaConverterJson.class)
  private List<PackageParam> defaultParams;
  @Column(columnDefinition = "TEXT")
  @Convert(converter = OutParamJpaConverterJson.class)
  private List<PackageOutParam> outParams;
  @Enumerated(EnumType.STRING)
  private PluginStatus pluginStatus;
  private String tags;
  private String pkgPath;

}
