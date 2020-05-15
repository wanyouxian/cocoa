package com.rocky.cocoa.entity.visual;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.rocky.cocoa.entity.BaseEntity;
import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@JsonIgnoreProperties(value = { "hibernateLazyInitializer"})
public class ChartInfo extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String name;
  private String description;
  @Enumerated(EnumType.STRING)
  private ChartType chartType;
  @Column(columnDefinition = "TEXT")
  private String chartSpecific;
  private Long dashboardId;
  @Column(columnDefinition = "TEXT")
  @Convert(converter = LocationJpaConverterJson.class)
  private Location location;
}
