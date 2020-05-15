package com.rocky.cocoa.server.service;

import com.rocky.cocoa.entity.meta.DataSource;
import com.rocky.cocoa.entity.meta.DbInfo;
import com.rocky.cocoa.entity.meta.ProjectInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import java.io.IOException;
import java.util.List;

public interface MetaService {
    //创建projectinfo
    void createProjectInfo(ProjectInfo projectInfo) throws IOException, InterruptedException;

    //更新projectinfo
    void updateProjectInfo(ProjectInfo projectInfo) throws IOException, InterruptedException;

    //删除projectinfo
    void delProjectInfo(long id);

    //查询projectinfo
    ProjectInfo findProjectInfoById(long id);

    ProjectInfo findProjectInfoByName(String name);

    List<String> listProjectNames(String team);

    //分页查询
    Page<ProjectInfo> listProjectInfos(String team, int page, int size, String sort, Sort.Direction direction);

    void createDbInfo(DbInfo dbInfo) throws IOException, InterruptedException;

    void updateDbInfo(DbInfo dbInfo);

    void delDbInfo(long id);

    DbInfo findDbInfoById(long id);

    DbInfo findDbInfoByName(String name);

    List<DbInfo> findDbInfoByProjectName(String name);

    List<DbInfo> findDbInfoByProjectId(long id);

    Page<DbInfo> listDbInfos(String team, int page, int size, String sort, Sort.Direction direction);

    List<String> showTables(String dbName);

    Object getTableSchema(String dbName, String tableName);

    void createDataSource(DataSource dataSource);

    void updateDataSource(DataSource dataSource);

    void delDataSource(long id);

    DataSource findDataSourceById(long id);

    DataSource findDataSourceByName(String name);

    List<DataSource> findDataSourceByProjectName(String name);

    List<DataSource> findDataSourceByProjectId(long id);

    Page<DataSource> listDataSources(String team, int page, int size, String sort, Sort.Direction direction);

}
