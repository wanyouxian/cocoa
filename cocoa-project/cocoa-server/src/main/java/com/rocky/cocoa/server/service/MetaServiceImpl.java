package com.rocky.cocoa.server.service;

import cn.hutool.core.util.ObjectUtil;
import com.rocky.cocoa.core.HadoopClient;
import com.rocky.cocoa.entity.meta.DataSource;
import com.rocky.cocoa.entity.meta.DbInfo;
import com.rocky.cocoa.entity.meta.ProjectInfo;
import com.rocky.cocoa.repository.meta.DataSourceRepository;
import com.rocky.cocoa.repository.meta.DbInfoRepository;
import com.rocky.cocoa.repository.meta.ProjectInfoRepository;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.client.HdfsAdmin;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MetaServiceImpl implements MetaService {
    @Value("${custom.hadoop.proxyuser}")
    private String proxyUser;
    @Value("${custom.hadoop.conf}")
    private String hadoopConfPath;
    @Value("${custom.hadoop.hivemetastore}")
    private String hiveMetaStoreUri;

    @Resource
    ProjectInfoRepository projectInfoRepository;
    @Resource
    DbInfoRepository dbInfoRepository;
    @Resource
    DataSourceRepository dataSourceRepository;

    @Override
    public void createProjectInfo(ProjectInfo projectInfo) throws IOException, InterruptedException {
        //创建hdfs目录
        String hdfsUri = String.format("hdfs://%s", projectInfo.getNs());

        HadoopClient hadoopClient = new HadoopClient(proxyUser, hadoopConfPath, hiveMetaStoreUri);
        FileSystem fileSystem = hadoopClient.getFileSystem(null, hdfsUri);
        if (!fileSystem.exists(new Path(projectInfo.getBasePath()))) {
            fileSystem.mkdirs(new Path(projectInfo.getBasePath()));
        }

        //设置hdfs配额
        HdfsAdmin hdfsAdmin = hadoopClient.getHdfsAdmin(hdfsUri);
        hdfsAdmin.setQuota(new Path(projectInfo.getBasePath()), projectInfo.getDsQuota());
        hdfsAdmin.setSpaceQuota(new Path(projectInfo.getBasePath()), projectInfo.getNsQuota());
        //设置权限
        projectInfoRepository.save(projectInfo);
    }

    @Override
    public void updateProjectInfo(ProjectInfo projectInfo) throws IOException, InterruptedException {
        //设置hdfs配额
//        String hdfsUri = String.format("hdfs://%s", projectInfo.getNs());
//        HadoopClient hadoopClient = new HadoopClient(proxyUser, hadoopConfPath, hiveMetaStoreUri);
//
//        HdfsAdmin hdfsAdmin = hadoopClient.getHdfsAdmin(hdfsUri);
//        hdfsAdmin.setQuota(new Path(projectInfo.getBasePath()), projectInfo.getDsQuota());
//        hdfsAdmin.setSpaceQuota(new Path(projectInfo.getBasePath()), projectInfo.getNsQuota());

        projectInfoRepository.save(projectInfo);
    }

    @Override
    public void delProjectInfo(long id) {
        ProjectInfo projectInfo = projectInfoRepository.findById(id).get();
        projectInfo.setIsTrash(true);
        projectInfoRepository.save(projectInfo);
    }

    @Override
    public ProjectInfo findProjectInfoById(long id) {
        return projectInfoRepository.findById(id).get();
    }

    @Override
    public ProjectInfo findProjectInfoByName(String name) {
        return projectInfoRepository.findByName(name);
    }

    @Override
    public List<String> listProjectNames(String team) {
        ProjectInfo projectInfo = new ProjectInfo();
        projectInfo.setIsTrash(false);
        projectInfo.setTeam(team);
        return projectInfoRepository.findAll(Example.of(projectInfo)).stream().map(ProjectInfo::getName).collect(Collectors.toList());
    }

    @Override
    public Page<ProjectInfo> listProjectInfos(String team, int page, int size, String sort, Sort.Direction direction) {
        ProjectInfo projectInfo = new ProjectInfo();
//        projectInfo.setIsTrash(false);
        projectInfo.setTeam(team);

        return projectInfoRepository.findAll(Example.of(projectInfo),
                PageRequest.of(page, size,
                        Sort.by(direction == null ? Sort.Direction.DESC : direction,
                                ObjectUtil.isNull(sort) ? "id" : sort)));
    }

    @Override
    public void createDbInfo(DbInfo dbInfo) throws IOException, InterruptedException {
        //创建hive database
//        HadoopClient hadoopClient = new HadoopClient(proxyUser, hadoopConfPath, hiveMetaStoreUri);
//        hadoopClient.createDataBase(dbInfo.getName(), dbInfo.getLocationUri(), dbInfo.getDetail(), null);
        dbInfoRepository.save(dbInfo);
    }

    @Override
    public void updateDbInfo(DbInfo dbInfo) {
        dbInfoRepository.save(dbInfo);
    }

    @Override
    public void delDbInfo(long id) {
        dbInfoRepository.deleteById(id);
    }

    @Override
    public DbInfo findDbInfoById(long id) {
        return dbInfoRepository.findById(id).orElse(null);
    }

    @Override
    public DbInfo findDbInfoByName(String name) {
        return dbInfoRepository.findByName(name);
    }

    @Override
    public List<DbInfo> findDbInfoByProjectName(String name) {
        return dbInfoRepository.findByProjectName(name);
    }

    @Override
    public List<DbInfo> findDbInfoByProjectId(long id) {
        return dbInfoRepository.findByProjectId(id);
    }

    @Override
    public Page<DbInfo> listDbInfos(String team, int page, int size, String sort, Sort.Direction direction) {

        DbInfo dbInfo = new DbInfo();
        dbInfo.setTeam(team);
        dbInfo.setIsTrash(false);
        return dbInfoRepository.findAll(Example.of(dbInfo),
                PageRequest.of(page, size,
                        Sort.by(direction == null ? Sort.Direction.DESC : direction,
                                ObjectUtil.isNull(sort) ? "id" : sort)));

    }

    @Override
    public List<String> showTables(String dbName) {
        HadoopClient hadoopClient = new HadoopClient(proxyUser, hadoopConfPath, hiveMetaStoreUri);

        return hadoopClient.showTables(dbName);
    }

    @Override
    public Object getTableSchema(String dbName, String tableName) {
        HadoopClient hadoopClient = new HadoopClient(proxyUser, hadoopConfPath, hiveMetaStoreUri);

        return hadoopClient.getTableSchemas(dbName, tableName);
    }

    @Override
    public void createDataSource(DataSource dataSource) {
        dataSourceRepository.save(dataSource);
    }

    @Override
    public void updateDataSource(DataSource dataSource) {
        dataSourceRepository.save(dataSource);
    }


    @Override
    public void delDataSource(long id) {
        dataSourceRepository.deleteById(id);
    }

    @Override
    public DataSource findDataSourceById(long id) {
        return dataSourceRepository.findById(id).orElse(null);
    }

    @Override
    public DataSource findDataSourceByName(String name) {
        return dataSourceRepository.findByName(name);
    }

    @Override
    public List<DataSource> findDataSourceByProjectName(String name) {
        return dataSourceRepository.findByProjectName(name);
    }

    @Override
    public List<DataSource> findDataSourceByProjectId(long id) {
        return dataSourceRepository.findByProjectId(id);
    }

    @Override
    public Page<DataSource> listDataSources(String team, int page, int size, String sort, Sort.Direction direction) {
        DataSource dataSource = new DataSource();
        dataSource.setTeam(team);
        dataSource.setIsTrash(false);
        return dataSourceRepository.findAll(Example.of(dataSource),
                PageRequest.of(page, size,
                        Sort.by(direction == null ? Sort.Direction.DESC : direction,
                                ObjectUtil.isNull(sort) ? "id" : sort)));
    }
}
