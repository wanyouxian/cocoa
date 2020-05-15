package com.rocky.cocoa.entity.visual;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.rocky.cocoa.entity.BaseEntity;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Data
@JsonIgnoreProperties(value = { "hibernateLazyInitializer"})
public class Dashboard extends BaseEntity {

  private String name;
  private String description;
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String creator;

  @Override
  public String toString(){
    return String.format("DashBoard %s", name);
  }
}
