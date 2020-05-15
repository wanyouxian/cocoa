package com.rocky.cocoa.core.client.azkaban.model;

import lombok.Data;

import java.util.List;

@Data
public class Node {
    private String id;
    private String nestedId;
    private String type;
    private Integer attempt;
    private String status;
    private Long updateTime;
    private Long startTime;
    private Long endTime;
    private List<String> in;
}
