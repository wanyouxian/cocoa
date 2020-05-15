package com.rocky.cocoa.plugins.demo;

import com.rocky.cocoa.sdk.AbstractCocoaPlugin;
import org.apache.spark.sql.SparkSession;

public class TestSparkPlugin extends AbstractCocoaPlugin<DemoConfig> {

    public static void main(String[] args) throws Exception {
        TestSparkPlugin sparkPlugin = new TestSparkPlugin();
        sparkPlugin.execute();
    }

    @Override
    public void execute() throws Exception {

        SparkSession sparkSession = SparkSession.builder().enableHiveSupport().getOrCreate();

        sparkSession.sql("show datbases");

        sparkSession.read().csv(this.getConfig().getInputFile());

        sparkSession.close();

    }
}
