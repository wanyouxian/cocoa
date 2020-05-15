package com.rocky.cocoa.plugins.demo;

import com.rocky.cocoa.sdk.PluginConfig;
import com.rocky.cocoa.sdk.PluginParam;
import lombok.Data;

@Data
public class DemoConfig implements PluginConfig {

    @PluginParam(name = "input.file.path")
    private String inputFile;


    @PluginParam(name = "output.file.path")
    private String outputFilePath;
}
