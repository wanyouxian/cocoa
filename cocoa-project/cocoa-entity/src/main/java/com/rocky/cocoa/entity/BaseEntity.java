package com.rocky.cocoa.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.MappedSuperclass;
import java.util.Date;

@Data
@MappedSuperclass
public class BaseEntity {
    @JsonIgnore
    private Boolean isTrash = false;
    private Date createTime;
}
