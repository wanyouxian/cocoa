package com.rocky.cocoa.core.client.azkaban.model;

import com.alibaba.fastjson.JSONArray;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Project {
    private String projectId;
    private String projectName;
    private String createBy;
    private Long createdTime;
    private JSONArray userPermissions;
    private JSONArray groupPermissions;
}
