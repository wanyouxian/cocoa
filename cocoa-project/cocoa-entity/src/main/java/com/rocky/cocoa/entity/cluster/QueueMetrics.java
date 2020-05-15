package com.rocky.cocoa.entity.cluster;

import com.rocky.cocoa.entity.BaseEntity;
import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name="queue_metrics")
public class QueueMetrics extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String queueName;
    private Integer appsPending;
    private Integer appsRunning;
    private Integer allocatedMB;
    private Integer availableMB;
    private Integer reservedMB;
    private Integer pendingMB;
    private Integer allocatedContainers;
    private Integer pendingContainers;
    private Integer ActiveUsers;
    private Integer metricsTime;
}
