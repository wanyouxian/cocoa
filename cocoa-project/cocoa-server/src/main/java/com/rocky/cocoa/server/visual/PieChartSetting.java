package com.rocky.cocoa.server.visual;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PieChartSetting implements ChartSetting {

    private String dimension;
    private String metrics;
    private boolean hoverAnimation = true;
    private int limitShowNum;
    private String[][] level;
    private String type = "pie";
    private String legendName;


    @Override
    public List<String> getColumns() {

        List<String> columns = new ArrayList<>();
        columns.add(dimension);
        columns.add(metrics);
        return columns;
    }
}
