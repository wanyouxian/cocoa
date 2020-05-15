package com.rocky.cocoa.server.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.google.common.base.Strings;
import com.rocky.cocoa.core.util.JsonUtil;
import com.rocky.cocoa.entity.visual.ChartInfo;
import com.rocky.cocoa.entity.visual.ChartType;
import com.rocky.cocoa.entity.visual.Location;
import com.rocky.cocoa.repository.visual.ChartInfoRepository;
import com.rocky.cocoa.server.jwt.ContextUtil;
import com.rocky.cocoa.server.visual.*;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

@Component
public class ChartServiceImpl implements ChartService {

    @Resource
    ChartInfoRepository chartInfoRepository;

    @Override
    public void saveChart(ChartInfoVo chartInfoVo) throws IOException {
        ChartInfo chartInfo = generateChartInfo(chartInfoVo);
        chartInfoRepository.save(chartInfo);
    }

    @Override
    public void saveChartInfo(ChartInfo chartInfo) throws IOException {
        chartInfoRepository.save(chartInfo);
    }

    private ChartInfo generateChartInfo(ChartInfoVo chartInfoVo) throws IOException {
        ChartInfo chartInfo = new ChartInfo();
        chartInfo.setCreateTime(new Date());
        chartInfo.setIsTrash(false);
        chartInfo.setDashboardId(chartInfoVo.getDashboardId());
        chartInfo.setChartType(chartInfoVo.getChartType());
        chartInfo.setDescription(chartInfoVo.getDescription());
        chartInfo.setName(chartInfoVo.getName());
        Location location = new Location();
        location.setWidgetH(chartInfoVo.getWidgetH());
        location.setWidgetX(chartInfoVo.getWidgetX());
        location.setWidgetY(chartInfoVo.getWidgetY());
        location.setWidgetW(chartInfoVo.getWidgetW());
        chartInfo.setLocation(location);
        String chartSpecific = JsonUtil.toJson(generateChartSpecific(chartInfoVo.getChartSpecific(), chartInfoVo.getChartType()));
        chartInfo.setChartSpecific(chartSpecific);
        return chartInfo;
    }

    private Object generateChartSpecific(Map<String, Object> chartSpecific, ChartType chartType) {

        switch (chartType) {
            case histogram:
                BarChartSpecific barChartSpecific = new BarChartSpecific();
                barChartSpecific.setChartType(ChartType.histogram);
                barChartSpecific.setCreator(ContextUtil.getCurrentUser().getName());
                barChartSpecific.setQuerySql(chartSpecific.get("sql").toString());
                barChartSpecific.setTitle(chartSpecific.getOrDefault("title", "").toString());
                BarChartSetting barChartSetting = new BarChartSetting();
                String dimension = chartSpecific.get("dimension").toString();
                String substring = dimension.substring(1, dimension.length() - 1);
                barChartSetting.setDimension(substring.split(","));
                String metrics = chartSpecific.get("metrics").toString();
                String metricsStr = metrics.substring(1, metrics.length() - 1);
                barChartSetting.setMetrics(metricsStr.split(","));
                barChartSetting.setLegendName(chartSpecific.getOrDefault("title", "").toString());
                barChartSetting.setShowLine(chartSpecific.getOrDefault("showLine", "").toString().split(","));
                barChartSetting.setXAxisType(Strings.emptyToNull(chartSpecific.getOrDefault("xAxisType", "").toString()));
                barChartSetting.setYAxisName(ObjectUtil.isNull(chartSpecific.get("yAxisName")) ? null : chartSpecific.get("yAxisName").toString().split(","));
                barChartSetting.setYAxisType(ObjectUtil.isNull(chartSpecific.get("yAxisType")) ? null : chartSpecific.get("yAxisType").toString().split(","));
                barChartSpecific.setChartSetting(barChartSetting);
                return barChartSpecific;
            case pie:
                PieChartSpecific pieChartSpecific = new PieChartSpecific();
                pieChartSpecific.setChartType(ChartType.pie);
                pieChartSpecific.setCreator(ContextUtil.getCurrentUser().getName());
                pieChartSpecific.setQuerySql(chartSpecific.get("sql").toString());
                pieChartSpecific.setTitle(chartSpecific.getOrDefault("title", "").toString());
                PieChartSetting pieChartSetting = new PieChartSetting();
                pieChartSetting.setDimension(chartSpecific.get("dimension").toString());
                pieChartSetting.setMetrics(chartSpecific.get("metrics").toString());
                pieChartSetting.setLegendName(chartSpecific.getOrDefault("title", "").toString());
                pieChartSetting.setHoverAnimation(Boolean.parseBoolean(chartSpecific.getOrDefault("hoverAnimation", "true").toString()));
                pieChartSetting.setLimitShowNum(Integer.parseInt(chartSpecific.getOrDefault("limitShowNum", "8").toString()));
                pieChartSpecific.setChartSetting(pieChartSetting);
                return pieChartSpecific;
            case line:
                LineChartSpecific lineChartSpecific = new LineChartSpecific();
                lineChartSpecific.setChartType(ChartType.line);
                lineChartSpecific.setCreator(ContextUtil.getCurrentUser().getName());
                lineChartSpecific.setQuerySql(chartSpecific.get("sql").toString());
                lineChartSpecific.setTitle(chartSpecific.getOrDefault("title", "").toString());
                LineChartSetting lineChartSetting = new LineChartSetting();
                String lineDimension = chartSpecific.get("dimension").toString();
                String lineDimensionStr = lineDimension.substring(1, lineDimension.length() - 1);
                lineChartSetting.setDimension(lineDimensionStr.split(","));
                String lineMetrics = chartSpecific.get("metrics").toString();
                String lineMetricStr = lineMetrics.substring(1, lineMetrics.length() - 1);
                lineChartSetting.setMetrics(lineMetricStr.split(","));
                lineChartSetting.setLegendName(chartSpecific.getOrDefault("title", "").toString());
                lineChartSetting.setArea(Boolean.parseBoolean(chartSpecific.getOrDefault("area", "false").toString()));
                lineChartSetting.setXAxisType(Strings.emptyToNull(chartSpecific.getOrDefault("xAxisType", "").toString()));
                lineChartSetting.setYAxisName(ObjectUtil.isNull(chartSpecific.get("yAxisName")) ? null : chartSpecific.get("yAxisName").toString().split(","));
                lineChartSetting.setYAxisType(ObjectUtil.isNull(chartSpecific.get("yAxisType")) ? null : chartSpecific.get("yAxisType").toString().split(","));
                lineChartSpecific.setChartSetting(lineChartSetting);
                return lineChartSpecific;
            default:
                break;
        }
        return null;
    }

    @Override
    public ChartInfo getChartById(long id) {
        return chartInfoRepository.getOne(id);
    }

    @Override
    public void deleteChart(long id) {
        chartInfoRepository.deleteById(id);
    }

    @Override
    public void updateChart(ChartInfoVo chartInfoVo) throws IOException {
        ChartInfo chartInfo = generateChartInfo(chartInfoVo);
        chartInfoRepository.save(chartInfo);
    }

    @Override
    public ChartSpecific convertChartSpecific(String chartSpecific, ChartType chartType) throws IOException {
        switch (chartType) {
            case line:
                return JsonUtil.fromJson(LineChartSpecific.class, chartSpecific);
            case pie:
                return JsonUtil.fromJson(PieChartSpecific.class, chartSpecific);
            case histogram:
                return JsonUtil.fromJson(BarChartSpecific.class, chartSpecific);
            default:
                return null;
        }
    }
}
