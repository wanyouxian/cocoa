package com.rocky.cocoa.server.visual;

import java.util.HashMap;
import java.util.Map;

public class EmptyChartSettingFactory {
    private static Map<String, Class> settingMap = new HashMap<>();

    static {
        settingMap.put("histogram", BarChartSetting.class);
        settingMap.put("pie", PieChartSetting.class);
        settingMap.put("line", LineChartSetting.class);
    }

    public static Class getEmptyChartSetting(String type) {
        return settingMap.get(type);
    }
}
