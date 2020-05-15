package com.rocky.cocoa.core.client.azkaban.model;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

@Data
public class Schedule {
    private String scheduleId;
    private String cronExpression;
    private String submitUser;
    private String firstSchedTime;
    private String nextExecTime;
    private String period;
    private JSONObject executionOptions;
}
