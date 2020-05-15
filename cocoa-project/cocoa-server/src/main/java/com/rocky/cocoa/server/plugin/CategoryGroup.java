package com.rocky.cocoa.server.plugin;

import lombok.Data;

import java.util.List;

@Data
public class CategoryGroup {
    private String id;
    private String name;
    private String type="group";
    private List<PluginMeta> children;
    private String ico = "el-icon-video-play";

    @Data
    public static class PluginMeta{
        private String id;
        private String name;
        private String type;
        private String ico = "el-icon-odometer";
    }
}
