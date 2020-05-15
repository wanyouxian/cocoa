package com.rocky.cocoa.server.controller;

import com.rocky.cocoa.core.exception.CocoaException;
import com.rocky.cocoa.core.exception.ErrorCodes;
import com.rocky.cocoa.entity.meta.DataSource;
import com.rocky.cocoa.entity.meta.DbInfo;
import com.rocky.cocoa.entity.meta.ProjectInfo;
import com.rocky.cocoa.server.BaseController;
import com.rocky.cocoa.server.jwt.ContextUtil;
import com.rocky.cocoa.server.jwt.LoginRequired;
import com.rocky.cocoa.server.log.OperationObj;
import com.rocky.cocoa.server.log.OperationRecord;
import com.rocky.cocoa.server.service.MetaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cocoa/v1/meta")
@CrossOrigin
public class MetaController extends BaseController {
    @Autowired
    MetaService metaService;

    //列出Projectinfo（分页）
    @ResponseBody
    @GetMapping("projects")
    @LoginRequired
    @OperationRecord("查询业务线列表")
    public Object listProjectInfos(@RequestParam(name = "pageIndex", required = false, defaultValue = "1") int pageIndex,
                                   @RequestParam(name = "pageSize", required = false, defaultValue = "20") int pageSize) {
        //team  需要获取当前登录用户的team
        Page<ProjectInfo> projectInfos = metaService.listProjectInfos(ContextUtil.getCurrentUser().getTeam(), pageIndex-1, pageSize, null, null);
        Map<String, Object> pages = new HashMap<>();
        pages.put("pages", projectInfos.getContent());
        pages.put("pageIndex", pageIndex);
        pages.put("pageSize", pageSize);
        pages.put("pageCount", projectInfos.getTotalPages());
        return getResult(pages);
    }

    @ResponseBody
    @GetMapping("project/name")
    @LoginRequired
    public Object listProjectNames() {
        System.out.println(ContextUtil.getCurrentUser().getName());
        List<String> strings = metaService.listProjectNames(ContextUtil.getCurrentUser().getTeam());
        return getResult(strings);
    }

    //创建project
    @ResponseBody
    @PostMapping("project")
    @LoginRequired
    @OperationRecord("创建业务线")
    public Object createProject(@RequestBody @OperationObj ProjectInfo projectInfo) throws IOException, InterruptedException {
        projectInfo.setIsTrash(false);
        projectInfo.setCreateTime(new Date());
        projectInfo.setAdmin(ContextUtil.getCurrentUser().getName());
        projectInfo.setTeam(ContextUtil.getCurrentUser().getTeam());
        projectInfo.setDsQuota(projectInfo.getDsQuota());
        metaService.createProjectInfo(projectInfo);
        return getResult(true);
    }


    @ResponseBody
    @PutMapping("project")
    @OperationRecord("更新业务线信息")
    @LoginRequired
    public Object updateProject(@RequestBody @OperationObj ProjectInfo projectInfo) throws IOException, InterruptedException {
        metaService.updateProjectInfo(projectInfo);
        return getResult(true);
    }

    @ResponseBody
    @GetMapping("project")
    @LoginRequired
    @OperationRecord("获取业务线详情")
    public Object getProject(@RequestParam @OperationObj Long id) {
        ProjectInfo projectInfoById = metaService.findProjectInfoById(id);
        return getResult(projectInfoById);
    }

    @ResponseBody
    @DeleteMapping("project")
    @OperationRecord("删除业务线")
    @LoginRequired
    public Object delProject(@RequestParam @OperationObj Long id) {
        metaService.delProjectInfo(id);
        return getResult(true);
    }


    //列出dbinfo 分页

    @ResponseBody
    @GetMapping("dbs")
    @LoginRequired
    @OperationRecord("查询数仓库列表")
    public Object listDbInfos(@RequestParam(name = "pageIndex", required = false, defaultValue = "1") int pageIndex,
                              @RequestParam(name = "pageSize", required = false, defaultValue = "20") int pageSize) {
        //team  需要获取当前登录用户的team
        Page<DbInfo> dbInfos = metaService.listDbInfos(ContextUtil.getCurrentUser().getTeam(), pageIndex-1, pageSize, null, null);
        Map<String,Object> pages = new HashMap<>();
        pages.put("pages",dbInfos.getContent());
        pages.put("pageIndex",pageIndex);
        pages.put("pageSize",pageSize);
        pages.put("pageCount",dbInfos.getTotalPages());
        return getResult(pages);
    }

    //创建db
    @ResponseBody
    @PostMapping("db")
    @LoginRequired
    @OperationRecord("创建数仓库")
    public Object createDb(@RequestBody @OperationObj DbInfo dbInfo) throws IOException, InterruptedException {
        ProjectInfo projectInfoByName = metaService.
                findProjectInfoByName(dbInfo.getProjectName());
        if (projectInfoByName == null) {
            return getError(ErrorCodes.ERROR_PARAM, "project not exists");
        }
        dbInfo.setTeam(ContextUtil.getCurrentUser().getTeam());
        dbInfo.setAdmin(ContextUtil.getCurrentUser().getName());
        dbInfo.setIsTrash(false);
        dbInfo.setCreateTime(new Date());
        dbInfo.setProjectId(projectInfoByName.getId());
        dbInfo.setLocationUri(projectInfoByName.getBasePath() +
                "/warehouse/" + dbInfo.getLevel() + "/" + dbInfo.getName() + ".db");
        metaService.createDbInfo(dbInfo);
        return getResult(true);
    }

    @ResponseBody
    @GetMapping("db")
    @LoginRequired
    @OperationRecord("获取数仓库详情")
    public Object getDb(@RequestParam @OperationObj Long id) {
        DbInfo dbInfoById = metaService.findDbInfoById(id);
        return getResult(dbInfoById);
    }

    @ResponseBody
    @DeleteMapping("db")
    @OperationRecord("删除数仓库")
    @LoginRequired
    public Object delDb(@RequestParam @OperationObj Long id) {
        metaService.delDbInfo(id);
        return getResult(true);
    }

    @ResponseBody
    @GetMapping("datasources")
    @LoginRequired
    @OperationRecord("列出数据源")
    public Object listDataSources(@RequestParam(name = "pageIndex", required = true, defaultValue = "1")
                                          int pageIndex,
                                  @RequestParam(name = "pageSize", required = true, defaultValue = "20")
                                          int pageSize) {
        Page<DataSource> dataSources = metaService.listDataSources(ContextUtil.getCurrentUser().getTeam(), pageIndex-1, pageSize, null, null);
        Map<String,Object> pages = new HashMap<>();
        pages.put("pages",dataSources.getContent());
        pages.put("pageIndex",pageIndex);
        pages.put("pageSize",pageSize);
        pages.put("pageCount",dataSources.getTotalPages());
        return getResult(pages);
    }

    @ResponseBody
    @PostMapping("datasource")
    @OperationRecord("创建数据源信息")
    @LoginRequired
    public Object createDataSource(@RequestBody @OperationObj DataSource dataSource) {
        ProjectInfo projectInfoByName = metaService.findProjectInfoByName(dataSource.getProjectName());
        if(projectInfoByName==null){
            throw new CocoaException("project not exists",ErrorCodes.ERROR_PARAM);
        }
        dataSource.setTeam(ContextUtil.getCurrentUser().getTeam());
        dataSource.setAdmin(ContextUtil.getCurrentUser().getName());
        dataSource.setIsTrash(false);
        dataSource.setCreateTime(new Date());
        dataSource.setProjectId(projectInfoByName.getId());
        metaService.createDataSource(dataSource);
        return getResult(true);
    }

    @ResponseBody
    @PutMapping("datasource")
    @LoginRequired
    @OperationRecord("更新数据源信息")
    public Object updateDataSource(@RequestBody @OperationObj DataSource dataSource) {
        metaService.updateDataSource(dataSource);
        return getResult(true);
    }

    @ResponseBody
    @GetMapping("datasource")
    @OperationRecord("获取数据源详情")
    @LoginRequired
    public Object getDataSource(@RequestParam @OperationObj Long id) {
        DataSource dataSourceById = metaService.findDataSourceById(id);
        return getResult(dataSourceById);
    }

    @ResponseBody
    @DeleteMapping("datasource")
    @LoginRequired
    @OperationRecord("删除数据源")
    public Object delDataSource(@RequestParam @OperationObj Long id) {
        metaService.delDataSource(id);
        return getResult(true);
    }
}
