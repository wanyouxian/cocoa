package com.rocky.cocoa.entity.query;

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
@Table(name = "saved_sql")
public class SavedSql extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String name;
  @Column(columnDefinition = "TEXT")
  private String sqlContent;
  private String creator;
}
