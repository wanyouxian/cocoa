package com.rocky.cocoa.sdk;

import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.security.UserGroupInformation;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.net.MalformedURLException;
import java.security.PrivilegedAction;
import java.util.Map;
import java.util.Properties;

@Slf4j
public class PluginContextFactory {

    private static PluginContext pluginContext;

    public static synchronized <T> PluginContext getOrCreateContext(
            Class<? extends AbstractCocoaPlugin> pluginClass) throws IOException {
        if (pluginContext != null) {
            return pluginContext;
        }
        pluginContext = createRemoteContext(pluginClass);
        return pluginContext;
    }

    public static PluginContext createRemoteContext(Class<? extends AbstractCocoaPlugin> pluginClass)
            throws IOException {
        DefaultPluginContext context = new DefaultPluginContext();
        fillRuntimeArgs(context);
        Properties runtime = new Properties();
        FileInputStream input = null;
        try {
            input = new FileInputStream("runtime.properties");
        } catch (IOException e) {
            input = new FileInputStream(String.format("%s/runtime.properties", context.getJobName()));
        }
        runtime.load(input);
        input.close();
        String apiServer = runtime.getProperty("api.server");
        String apiPort = runtime.getProperty("api.server.port");
        CocoaTaskApi cocoaTaskApi = new CocoaTaskApi(apiServer, apiPort);

        Map<String, Object> confMap = null;
        confMap = cocoaTaskApi
                .getJobConfig(context.getTaskName(), context.getJobName(), context.getFlowExecId());
        log.info(confMap.toString());
        context.principal = runtime.get("team").toString();
        context.cocoaTaskApi = cocoaTaskApi;
        try {
            Class<? extends PluginConfig> configClazz = (Class<? extends PluginConfig>)
                    ((ParameterizedType) pluginClass
                            .getGenericSuperclass()).getActualTypeArguments()[0];
            confMap = replaceReferenceJobOutparams(context, confMap, cocoaTaskApi);
            context.config = PluginConfigParser.parseConfig(configClazz, confMap);
        } catch (Exception e) {
            throw new RuntimeException("parse job config error", e);
        }
        context.hadoopHome = System.getenv("HADOOP_HOME");
        if (context.hadoopHome == null) {
            context.hadoopHome = System.getProperty("HADOOP_HOME");
        }
        if (context.hadoopHome == null) {
            log.warn("HADOOP_HOME not set");
        } else {
            log.info("HADOOP_HOME checked:" + context.hadoopHome);
        }

        Configuration config = new Configuration();
        try {
            config.addResource(
                    new File(context.getHadoopHome() + "/etc/hadoop/core-site.xml").toURI().toURL());
            config.addResource(
                    new File(context.getHadoopHome() + "/etc/hadoop/hdfs-site.xml").toURI().toURL());
            config.set("fs.hdfs.impl",
                    org.apache.hadoop.hdfs.DistributedFileSystem.class.getName()
            );
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        context.hdfsConfiguration = config;
        cocoaTaskApi.saveJobRuntimeConfig(context.getTaskName(), context.getJobName(),
                context.getFlowExecId(), confMap);
        return context;
    }

    private static String getEnvOrProperty(String key) {
        Properties sysProperties = System.getProperties();
        if (sysProperties.containsKey(key)) {
            return sysProperties.getProperty(key);
        } else if (System.getenv() != null && System.getenv().containsKey(key)) {
            return System.getenv(key);
        }
        return null;
    }

    private static void fillRuntimeArgs(DefaultPluginContext context) {
        //azkaban.flowid=%s -Dazkaban.execid=%s -Dazkaban.jobid=%s -Dazkaban.jobname
        setJavaOptionsToSysProperty();
        String flowName = getEnvOrProperty("azkaban.flowid");
        String runtimeJobId = getEnvOrProperty("azkaban.jobid");
        context.flowName = flowName;
        String execId = getEnvOrProperty("azkaban.execid");
        context.flowExecId = execId;
        context.runtimeJobId = runtimeJobId;
        String jobName = getEnvOrProperty("azkaban.jobname");
        String taskName = getEnvOrProperty("azkaban.projectname");
        context.jobName = jobName;
        context.taskName = taskName;
        System.out.println("plugin runtime info :flowName="
                + flowName + " execId=" + execId + "  taskName=" + taskName + " jobName=" + jobName);

    }

    private static void setJavaOptionsToSysProperty() {
        //spark.executor.extraJavaOptions="-Dazkaban.flowid=spark_rpc
        // -Dazkaban.execid=12986 -Dazkaban.jobid=spark_rpc
        // -Dazkaban.jobname=spark_rpc -Dazkaban.projectname=spark_rpc_param_test3"
        String extraJavaOptions = getEnvOrProperty("spark.executor.extraJavaOptions");
        if (extraJavaOptions == null) {
            log.info("spark.executor.extraJavaOptions not set");
            return;
        }
        extraJavaOptions = extraJavaOptions.replaceAll("-D", "")
                .replaceAll("\"", "");
        String[] pairs = extraJavaOptions.split(" ");
        for (String pair : pairs) {
            String[] ss = pair.split("=");
            if (ss.length == 2) {
                System.setProperty(ss[0], ss[1]);
            }
        }

    }

    private static Map<String, Object> replaceReferenceJobOutparams(PluginContext context,
                                                                    Map<String, Object> confMap, CocoaTaskApi cocoaTaskApi) throws IOException {
        Map<String, Map<String, Object>> params =
                cocoaTaskApi.getTaskOutputParams(context.getTaskName(), context.getFlowExecId());
        for (String key : confMap.keySet()) {
            if (confMap.get(key) == null) {
                continue;
            }
            String value = confMap.get(key).toString().trim();
            if (value.matches("^\\$\\{.+\\}$")) {
                String p = value.substring(2, value.length() - 1);
                String[] ss = p.split("\\.", 2);
                String jobname = ss[0];
                String paramname = ss[1];
                if (params.get(jobname) == null
                        || params.get(jobname).get(paramname) == null) {
                    throw new RuntimeException("no output param found for job :" + jobname);
                }
                confMap.put(key, params.get(jobname).get(paramname));
            }
        }
        return confMap;
    }

    static class DefaultPluginContext implements PluginContext {

        private String principal;
        private PluginConfig config;
        private String hadoopHome;
        private String taskName;
        private String jobName;
        private String runtimeJobId;
        private String flowExecId;
        private String flowName;
        private FileSystem fileSystem;
        private UserGroupInformation userGroupInformation;
        private Configuration hdfsConfiguration;
        private CocoaTaskApi cocoaTaskApi;

        public DefaultPluginContext() {

        }

        private void checkFileSystem() throws IOException {
            if (this.userGroupInformation == null) {
                UserGroupInformation.setConfiguration(hdfsConfiguration);
                this.userGroupInformation = UserGroupInformation.createRemoteUser(principal);
                UserGroupInformation.setLoginUser(userGroupInformation);
            }
            if (fileSystem == null) {
                fileSystem = userGroupInformation.doAs(new PrivilegedAction<FileSystem>() {
                    @Override
                    public FileSystem run() {
                        try {
                            FileSystem fs = FileSystem
                                    .get(hdfsConfiguration);
                            return fs;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                });
            } else {
                userGroupInformation.checkTGTAndReloginFromKeytab();
            }
        }


        @Override
        public String getPrincipal() {
            return this.principal;
        }

        public PluginConfig getConfig() {
            return this.config;
        }

        public String getHadoopHome() {
            return this.hadoopHome;
        }

        public FileSystem getFileSystem() throws IOException {
            checkFileSystem();
            return this.fileSystem;
        }


        @Override
        public String getTaskName() {
            return this.taskName;
        }

        @Override
        public String getFlowName() {
            return flowName;
        }

        @Override
        public String getRuntimeJobId() {
            return runtimeJobId;
        }

        @Override
        public String getJobName() {
            return this.jobName;
        }

        @Override
        public String getFlowExecId() {
            return this.flowExecId;
        }

        @Override
        public String getScheduleId() {
            return null;
        }

        @Override
        public void outputValues(PluginOutputValues outpuValues) throws IOException {
            cocoaTaskApi.saveOutputParams(pluginContext.getTaskName(),
                    pluginContext.getJobName(),
                    pluginContext.getFlowExecId(),
                    outpuValues.getValues());
        }

        @Override
        public void saveTableInfo(String taskName, String dsName, String dbName, String tableName, String tableSchema) throws IOException {
            cocoaTaskApi.saveTableInfo(taskName, dsName, dbName, tableName, tableSchema);

        }

        @Override
        public void saveTableInfo(String dsName, String dbName, String tableName, String tableSchema) throws IOException {
            this.saveTableInfo(pluginContext.getTaskName(),dsName,dbName,tableName,tableSchema);

        }

    }
}
