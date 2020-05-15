package com.rocky.cocoa.server.visual;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.List;

public interface ChartSetting extends Serializable {
    @JsonIgnore
    List<String> getColumns();
}
