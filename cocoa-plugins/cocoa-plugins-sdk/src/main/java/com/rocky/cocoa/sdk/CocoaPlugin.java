package com.rocky.cocoa.sdk;

public interface CocoaPlugin {
    public PluginContext getContext();

    public void execute() throws Exception;
}
