package com.rocky.cocoa.server.service;

import com.rocky.cocoa.entity.visual.ChartInfo;
import com.rocky.cocoa.entity.visual.ChartType;
import com.rocky.cocoa.server.visual.ChartInfoVo;
import com.rocky.cocoa.server.visual.ChartSpecific;

import java.io.IOException;

public interface ChartService {
    //多是对于图表的增删改查
    void saveChart(ChartInfoVo chartInfo) throws IOException;

    void saveChartInfo(ChartInfo chartInfo) throws IOException;

    ChartInfo getChartById(long id);

    void deleteChart(long id);

    void updateChart(ChartInfoVo chartInfo) throws IOException;

    ChartSpecific convertChartSpecific(String chartSpecific, ChartType chartType) throws IOException;

    //获取图表chartspecific

}
