package com.rocky.cocoa.server.visual;

import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
public class BarChartSetting implements ChartSetting {
    //维度
    private String[] dimension;
    //指标
    private String[] metrics;
    //x轴数据类型
    private String xAxisType = "category";
    //左右y轴数据类型
    private String[] yAxisType;
    //左右y轴名称
    private String[] yAxisName;
    //以折线形式展示的指标
    private String[] showLine;

    private String type = "histogram";
    private String legendName;

    @Override
    public List<String> getColumns() {
        List<String> columns = new ArrayList<>();
        columns.addAll(Arrays.asList(dimension));
        columns.addAll(Arrays.asList(metrics));
        return columns;
    }
}
