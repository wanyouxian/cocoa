package com.rocky.cocoa.server.query.cache;

import com.google.common.base.Ticker;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.rocky.cocoa.server.query.dataframe.PagedDataFrame;

import java.util.concurrent.TimeUnit;

public class DataFrameCache {
    private static LoadingCache<String, PagedDataFrame> dataFrameLoadingCache;

    static {
        dataFrameLoadingCache = CacheBuilder.newBuilder().maximumSize(100)
                .expireAfterAccess(5, TimeUnit.MINUTES)
                .ticker(Ticker.systemTicker())
                .build(new CacheLoader<String, PagedDataFrame>() {
                    @Override
                    public PagedDataFrame load(String s) throws Exception {
                        return new PagedDataFrame();
                    }
                });
    }

    public static LoadingCache<String, PagedDataFrame> getDataFrameLoadingCache() {
        return dataFrameLoadingCache;
    }
}
