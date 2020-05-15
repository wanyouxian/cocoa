package com.rocky.cocoa.entity.system;

import com.rocky.cocoa.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Proxy;

import javax.persistence.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "cocoa_user")
@Proxy(lazy = false)
public class User  extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String name;
  private String pwd;
  private String mail;
  private String phone;
  private String team;

  @Override
  public String toString(){
    return String.format("User %s", name);
  }
}
