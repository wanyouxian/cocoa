package com.rocky.cocoa.sdk;

public abstract class AbstractCocoaPlugin<T extends PluginConfig> implements CocoaPlugin {

  private PluginContext pluginContext;

  public AbstractCocoaPlugin() {
    this.init();
  }

  protected void init() {
    try {
      pluginContext = PluginContextFactory.getOrCreateContext(this.getClass());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public PluginContext getContext() {
    return this.pluginContext;
  }

  public T getConfig() {
    return (T) this.pluginContext.getConfig();
  }


}
