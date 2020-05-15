package com.rocky.cocoa.server.service;

import cn.hutool.core.util.ObjectUtil;
import com.google.common.base.Joiner;
import com.rocky.cocoa.core.exception.ErrorCodes;
import com.rocky.cocoa.core.exception.CocoaException;
import com.rocky.cocoa.core.util.FileUtil;
import com.rocky.cocoa.entity.plugin.PluginCategory;
import com.rocky.cocoa.entity.plugin.PluginPackage;
import com.rocky.cocoa.repository.plugin.PluginPackageRepository;
import com.rocky.cocoa.server.plugin.CategoryGroup;
import com.rocky.cocoa.server.plugin.PluginPackageParser;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PluginServiceImpl implements PluginService {

    @Value("${custom.task.packageDir}")
    private String packageDir;

    @Resource
    PluginPackageRepository pluginPackageRepository;

    @PostConstruct
    public void init() {
        File file = new File(packageDir);
        if (file.exists() && file.isFile()) {
            throw new CocoaException("packagedir is a file", ErrorCodes.SYSTEM_EXCEPTION);
        }
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    @Override
    public PluginPackage savePluginFile(InputStream inputStream, String pkgName, String pkgVersion) throws IOException {
        //保持inputstream到tmp目录
        String storeDir = Joiner.on(File.separator).join(this.packageDir, "tmp", pkgName, pkgVersion);
        String pluginDir = Joiner.on(File.separator).join(this.packageDir, pkgName);
        File dir = new File(storeDir);
        if (dir.exists()) {
            FileUtil.deleteFileOrDir(dir);
        }
        dir.mkdirs();
        File file = new File(storeDir + File.separator + pkgName + "-" + pkgVersion + ".zip");
        if (file.exists()) {
            file.delete();
        }
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        IOUtils.copy(inputStream, fileOutputStream);
        fileOutputStream.close();
        inputStream.close();
        //调用parser方法 生成plugin package实体类
        PluginPackage pluginPackage = PluginPackageParser.parse(file, storeDir);
        file.delete();
        //将插件程序 移动到plugin dir
        if (pluginPackage.getName().equals(pkgName) && pluginPackage.getVersion().equals(pkgVersion)) {
            FileUtil.copyFileOrDirectory(dir, pluginDir, pkgVersion);
            pluginPackage.setPkgPath(pluginDir + File.separator + pkgVersion);
        } else {
            throw new CocoaException("pkg name or version is error", ErrorCodes.ERROR_PARAM);
        }
        FileUtil.deleteFileOrDir(dir);
        return pluginPackage;
    }

    @Override
    public PluginPackage getPluginByNameAndVersion(String name, String version) {
        return pluginPackageRepository
                .findByNameAndVersion(name, version);
    }

    @Override
    public Page<PluginPackage> getPlugins(int page, int size, String sort, Sort.Direction direction) {
        PluginPackage pluginPackage = new PluginPackage();
        pluginPackage.setIsTrash(false);
        return pluginPackageRepository.findAll(Example.of(pluginPackage),
                PageRequest.of(page, size,
                        Sort.by(direction == null ? Sort.Direction.DESC : direction, ObjectUtil.isNull(sort) ? "id" : sort)));

    }

    @Override
    public Object getPluginGroupBy() {
        PluginPackage pluginPackage = new PluginPackage();
        pluginPackage.setIsTrash(false);
        List<PluginPackage> all = pluginPackageRepository.findAll(Example.of(pluginPackage));
        Map<PluginCategory, List<PluginPackage>> pluginCategoryListMap = all.stream().collect(Collectors.groupingBy(PluginPackage::getPluginCategory));
        List<CategoryGroup> result = new ArrayList<>();
        Integer id = 1;
        for (PluginCategory category: pluginCategoryListMap.keySet()){
            CategoryGroup categoryGroup = new CategoryGroup();
            categoryGroup.setName(category.name());
            categoryGroup.setId(id.toString());
            List<CategoryGroup.PluginMeta> pluginMetas = pluginCategoryListMap.get(category).stream().map(pluginPackage1 -> {
                CategoryGroup.PluginMeta pluginMeta = new CategoryGroup.PluginMeta();
                pluginMeta.setId(pluginPackage1.getId().toString());
                pluginMeta.setName(String.format("%s-%s", pluginPackage1.getName(), pluginPackage1.getVersion()));
                pluginMeta.setType(String.format("%s-%s", pluginPackage1.getName(), pluginPackage1.getVersion()));
                return pluginMeta;
            }).collect(Collectors.toList());
            categoryGroup.setChildren(pluginMetas);
            result.add(categoryGroup);
            id++;
        }
        return result;
    }

    @Override
    public PluginPackage getPlugin(long id) {
        return pluginPackageRepository.findById(id).get();
    }

    @Override
    public void delPlugin(long id) throws IOException {
        PluginPackage pluginPackage = pluginPackageRepository.findById(id).get();
        removePluginFile(pluginPackage.getName(), pluginPackage.getVersion());
        pluginPackageRepository.delete(pluginPackage);
    }

    @Override
    public void delPlugin(String name, String version) throws IOException {
        PluginPackage pluginPackage = pluginPackageRepository.findByNameAndVersion(name, version);
        removePluginFile(name, version);
        pluginPackageRepository.delete(pluginPackage);
    }

    @Override
    public List<PluginPackage> getPluginsByName(String name) {
        return pluginPackageRepository.findByName(name);
    }

    @Override
    public void update(PluginPackage pluginPackage) {

        pluginPackageRepository.save(pluginPackage);
    }

    private void removePluginFile(String name, String version) throws IOException {
        FileUtil.deleteFileOrDir(new File(Joiner.on(File.separator).join(packageDir, name, version)));
    }

    @Override
    public String getPackageStoreLocation() {
        return this.packageDir;
    }

}
