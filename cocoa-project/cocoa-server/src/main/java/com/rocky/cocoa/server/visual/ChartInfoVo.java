package com.rocky.cocoa.server.visual;

import com.rocky.cocoa.entity.visual.ChartType;
import lombok.Data;

import java.util.Map;

@Data
public class ChartInfoVo {
    private String name;
    private String description;
    private ChartType chartType;
    private Map<String, Object> chartSpecific;
    private long dashboardId;
    private int widgetX;
    private int widgetY;
    private int widgetW;
    private int widgetH;

    @Override
    public String toString(){
        return String.format("Chart %s", name);
    }
}
