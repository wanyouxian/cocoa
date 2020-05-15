package com.rocky.cocoa.server.visual;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
public class ChartData implements Serializable {
    private List<String> columns;
    private List<Map<String, Object>> rows;
}
