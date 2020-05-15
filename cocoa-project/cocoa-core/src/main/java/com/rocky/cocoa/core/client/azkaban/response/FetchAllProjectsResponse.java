package com.rocky.cocoa.core.client.azkaban.response;

import com.rocky.cocoa.core.client.azkaban.model.Project;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FetchAllProjectsResponse extends BaseResponse {
    private List<Project> projects;
}