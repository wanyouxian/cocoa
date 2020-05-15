package com.rocky.cocoa.server.query.cache;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CacheThreadPool {

  private static Executor executor;

  private static final CacheThreadPool INSTANCE = new CacheThreadPool();


  private CacheThreadPool() {
    executor = Executors.newFixedThreadPool(20);
  }

  public static CacheThreadPool getInstance() {
    return INSTANCE;
  }

  public void execJob(Runnable runnable) {
    executor.execute(runnable);
  }

}
