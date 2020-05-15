package com.rocky.cocoa.server.plugin;

import com.rocky.cocoa.core.util.JsonUtil;
import com.rocky.cocoa.entity.plugin.PackageParam;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.util.*;

public class JobTypeParamSchemas {

    private static Map<String, List<PackageParam>> paramSchemas = new HashMap<>();
    private static Set<String> jobTypeInternalParams = new HashSet<String>();

    /**
     * load job type param schemas from class path.
     */
    static {
        String[] internalParams = new String[]{"type", "dependencies", "name", "user.to.proxy"};
        for (String p : internalParams) {
            jobTypeInternalParams.add(p);
        }
        PathMatchingResourcePatternResolver resourcePatternResolver =
                new PathMatchingResourcePatternResolver();
        try {
            Resource[] resources = resourcePatternResolver
                    .getResources("classpath:META-INF/params/*");
            for (Resource res : resources) {
                String fileName = res.getFilename();
                String[] ss = fileName.split("/");
                String name = ss[ss.length - 1];
                name = name.split("\\.")[0];
                String json = new String(IOUtils.toByteArray(res.getInputStream()));
                List<PackageParam> schemas = JsonUtil.fromJsonList(PackageParam.class, json);
                paramSchemas.put(name, schemas);
            }
        } catch (Exception ioe) {
            ioe.printStackTrace();
            throw new RuntimeException(ioe);
        }
    }

    private JobTypeParamSchemas() {
    }

    public static List<PackageParam> getJobTypeParamSchemas(String jobtype, String language) {
        List<PackageParam> list = new ArrayList<>();

        String key = jobtype;
        if (jobtype.equals("spark")) {
            key = jobtype + "-" + language;
        }
        list.addAll(paramSchemas.get(key));
        return list;
    }

    public static boolean isUserSetAble(String jobType, String language, String name) {
        for (PackageParam param : getJobTypeParamSchemas(jobType, language)) {
            if (param.getName().equals(name) && param.isUserSetAble()) {
                return true;
            }
        }
        return false;
    }

    public static Map<String, String> getJobTypeDefaultParams(String jobtype, String language) {
        List<PackageParam> list = getJobTypeParamSchemas(jobtype, language);
        Map<String, String> map = new HashMap<>();
        for (PackageParam schema : list) {
            if (schema.getDefaultValue() != null && schema.getDefaultValue().length() > 0) {
                map.put(schema.getName(), schema.getDefaultValue());
            }
        }
        return map;
    }

    public static boolean isInternalParam(String name) {
        return jobTypeInternalParams.contains(name);
    }

}
