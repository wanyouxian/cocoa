package com.rocky.cocoa.server.query;

public enum EngineType {
    PRESTO("com.facebook.presto.jdbc.PrestoDriver"),
    HIVE("org.apache.hive.jdbc.HiveDriver");

    private String driver;

    private EngineType(String driver) {
        this.driver = driver;
    }

    public String getDriver() {
        return driver;
    }
}
