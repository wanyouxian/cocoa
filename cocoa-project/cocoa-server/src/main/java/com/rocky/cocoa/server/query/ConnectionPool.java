package com.rocky.cocoa.server.query;

import com.google.common.base.Splitter;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class ConnectionPool {
    @Value("${custom.presto.uri}")
    private String prestoUri;
    @Value("${custom.hive.uri}")
    private String hiveUri;

    private static LoadingCache<String, Connection> cache = null;

    @PostConstruct
    public void init() {
        cache = CacheBuilder.newBuilder()
                //设置缓存池的大小，当缓存项超过该值时，则释放掉原有的链接
                .maximumSize(100)
                //设置对象没有进行读写超过5分钟则进行释放
                .expireAfterAccess(5, TimeUnit.MINUTES)
                .removalListener((RemovalListener<String, Connection>) removalNotification -> {
                    if (removalNotification.getValue() != null) {
                        try {
                            log.info("remove connection");
                            removalNotification.getValue().close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                })
                .recordStats()
                .build(new CacheLoader<String, Connection>() {
                    @Override
                    public Connection load(String key) throws Exception {
                        List<String> connInfo = Splitter.on("|").splitToList(key);
                        assert connInfo.size() == 2;
                        Class.forName(EngineType.valueOf(connInfo.get(1)).getDriver());
                        log.info("添加链接 foruser {} enginetype is {}", connInfo.get(0), connInfo.get(1));

                        Connection connection = null;
                        if (EngineType.valueOf(connInfo.get(1)).equals(EngineType.HIVE)) {
                            connection = DriverManager.getConnection(hiveUri, connInfo.get(0), null);
                        } else if (EngineType.valueOf(connInfo.get(1)).equals(EngineType.PRESTO)) {
                            connection = DriverManager.getConnection(prestoUri, connInfo.get(0), null);
                        }
                        return connection;
                    }
                });

    }

    public static Connection getConnection(String key) throws ExecutionException {
        // key     username|engineType

        return cache.get(key);
    }
}
