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
@Table(name = "query_execution")
public class QueryExecution extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(columnDefinition = "TEXT")
    private String execSql;
    private Integer status;
    private Integer startTime;
    private Float takeTime;
    private String creator;
    private String execEngine;
    private String queryId;
}
