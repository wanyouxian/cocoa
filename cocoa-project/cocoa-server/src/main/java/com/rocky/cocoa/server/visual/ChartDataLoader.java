package com.rocky.cocoa.server.visual;

import java.util.concurrent.ExecutionException;

public interface ChartDataLoader {
    ChartData load(ChartSpecific chartSpecific) throws ExecutionException, InterruptedException;

    ChartData loadData(String currentUser, String sql) throws ExecutionException, InterruptedException;
}
