package com.rocky.cocoa.server.schedule;

import cn.hutool.http.ContentType;
import cn.hutool.http.Header;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.google.common.base.Ticker;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.rocky.cocoa.core.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class YarnStatusCache {

    @Value("${custom.hadoop.rm.uri}")
    private String rmUriStr;
    private String SCHEDULER_URI_FORMAT = "http://%s/ws/v1/cluster/scheduler";
    private String METRICS_URI_FORMAT = "http://%s/ws/v1/cluster/metrics";
    private String APPS_URI_FORMAT = "http://%s/ws/v1/cluster/apps?states=%s&limit=%s&deSelects"
            + "=resourceRequests";

    private LoadingCache<String, Object> yarnRealTimeInfoLoadingCache;

    @PostConstruct
    public void init() {
        yarnRealTimeInfoLoadingCache = CacheBuilder.newBuilder().maximumSize(100)
                .expireAfterWrite(1, TimeUnit.MINUTES).ticker(Ticker.systemTicker())
                .build(new CacheLoader<String, Object>() {
                    @Override
                    public Object load(String key) throws Exception {
                        String[] split = key.split("_");
                        String activeRmUri = getUsefulRmUri();
                        switch (split[0].toLowerCase()) {
                            case "scheduler":
                                try {
                                    String schedulerUri = String.format(SCHEDULER_URI_FORMAT, activeRmUri);
                                    String body = HttpUtil.createGet(schedulerUri)
                                            .header(Header.ACCEPT, ContentType.JSON.toString()).execute().body();

                                    return JsonUtil.fromJson(LinkedHashMap.class, body);
                                } catch (Exception e) {
                                    log.error(e.getMessage());
                                }
                            case "apps":
                                try {
                                    String appsUri = String.format(APPS_URI_FORMAT, activeRmUri, split[1], split[2]);
                                    String appBody = HttpUtil.createGet(appsUri)
                                            .header(Header.ACCEPT, ContentType.JSON.toString()).execute().body();
                                    JSONObject jsonObject = JSONUtil.parseObj(appBody, true);
                                    JSONObject apps = JSONUtil.createObj();
                                    jsonObject.getJSONObject("apps").getJSONArray("app").forEach(item -> {
                                        apps.append("content", item);
                                    });
                                    apps.put("resourceManager", activeRmUri);
                                    return apps;
                                } catch (Exception e) {
                                    log.error(e.getMessage());
                                }
                            case "metrics":
                                try {
                                    String metricsUri = String.format(METRICS_URI_FORMAT, activeRmUri);
                                    String metricsBody = HttpUtil.createGet(metricsUri)
                                            .header(Header.ACCEPT, ContentType.JSON.toString()).execute().body();
                                    return JsonUtil.fromJson(LinkedHashMap.class, metricsBody);
                                } catch (Exception e) {
                                    log.error(e.getMessage());
                                }
                        }
                        return JSONUtil.parseObj("{}", true);
                    }
                });
    }

    private String getUsefulRmUri() throws IOException {
        List<String> rmUris = Arrays.asList(rmUriStr.split(";"));
        String activeRmUri = rmUris.get(0);
        if (rmUris.size() > 1) {
            for (String uri : rmUris) {
                try {
                    log.debug("Checking RM URL: " + uri);
                    String rmBody = HttpUtil.createGet(String.format("http://%s/ws/v1/cluster/info", uri))
                            .header(Header.ACCEPT, ContentType.JSON.toString()).execute().body();
                    JSONObject rootNode = JSONUtil.parseObj(rmBody, true);
                    String status = rootNode.getJSONObject("clusterInfo").getStr("haState");
                    if (status.equals("ACTIVE")) {
                        log.debug(uri + " is ACTIVE");
                        activeRmUri = uri;
                        break;
                    } else {
                        log.debug(uri + " is STANDBY");
                    }
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
            }
        }
        return activeRmUri;
    }


    public LoadingCache<String, Object> getLoadingCache() {
        return yarnRealTimeInfoLoadingCache;
    }

}
