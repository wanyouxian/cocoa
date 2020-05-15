package com.rocky.cocoa.server.schedule;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class HadoopMetrics {
    List<Map<String, Object>> beans = new ArrayList<>();

    public Object getMetricsValue(String name) {
        if (beans.isEmpty()) {
            return null;
        }
        return beans.get(0).getOrDefault(name, null);
    }
}
