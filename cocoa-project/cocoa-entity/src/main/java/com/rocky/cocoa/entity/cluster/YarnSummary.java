package com.rocky.cocoa.entity.cluster;


import com.rocky.cocoa.entity.BaseEntity;
import lombok.Data;
import lombok.ToString;

import javax.persistence.*;

@Entity
@Data
@ToString
@Table(name = "yarn_summary")
public class YarnSummary extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer liveNodeManagerNums;
    private Integer deadNodeManagerNums;
    private Integer unhealthyNodeManagerNums;
    private Integer submittedApps;
    private Integer runningApps;
    private Integer pendingApps;
    private Integer completedApps;
    private Integer killedApps;
    private Integer failedApps;
    private Long allocatedMem;
    private Integer allocatedCores;
    private Integer allocatedContainers;
    private Long availableMem;
    private Integer availableCores;
    private Long pendingMem;
    private Integer pendingCores;
    private Integer pendingContainers;
    private Long reservedMem;
    private Integer reservedCores;
    private Integer reservedContainers;
}
