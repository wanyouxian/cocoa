package com.rocky.cocoa.core.client.azkaban.response;

import com.rocky.cocoa.core.client.azkaban.model.Schedule;
import lombok.Data;

@Data
public class FetchScheduleResponse extends BaseResponse {
    private Schedule schedule;

}