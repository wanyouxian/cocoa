package com.rocky.cocoa.server.service;

import com.rocky.cocoa.entity.plugin.PluginPackage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface PluginService {
    //保持plugin file, 对zip 进行解压缩， 解析meta.json 将plugin信息保持到数据库，将插件保存到本地
    PluginPackage savePluginFile(InputStream inputStream, String pkgName, String pkgVersion) throws IOException;

    //获取plugin列表
    Page<PluginPackage> getPlugins(int page, int size, String sotr, Sort.Direction direction);

    Object getPluginGroupBy();

    //获取plugin详情
    PluginPackage getPlugin(long id);

    PluginPackage getPluginByNameAndVersion(String name, String version);

    //更新plugin
    void update(PluginPackage pluginPackage);

    //删除plugin
    void delPlugin(long id) throws IOException;

    void delPlugin(String name, String version) throws IOException;

    List<PluginPackage> getPluginsByName(String name);

    String getPackageStoreLocation();
}
