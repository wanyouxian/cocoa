package com.rocky.cocoa.server.visual;

import com.rocky.cocoa.entity.visual.ChartType;
import lombok.Data;

@Data
public abstract class ChartSpecific<C extends ChartSetting> {
    protected String querySql;
    protected String creator;
    protected ChartType chartType;
    protected String title;
    protected C chartSetting;

}
