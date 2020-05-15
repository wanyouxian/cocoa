package com.rocky.cocoa.server.controller;

import cn.hutool.core.date.DateUtil;
import com.rocky.cocoa.entity.query.SavedSql;
import com.rocky.cocoa.server.BaseController;
import com.rocky.cocoa.server.jwt.ContextUtil;
import com.rocky.cocoa.server.jwt.LoginRequired;
import com.rocky.cocoa.server.query.DataCacheUtil;
import com.rocky.cocoa.server.query.EngineType;
import com.rocky.cocoa.server.query.QueryObject;
import com.rocky.cocoa.server.service.QueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/cocoa/v1/query")
@CrossOrigin
public class QueryController extends BaseController {

    @Autowired
    QueryService queryService;

    @ResponseBody
    @GetMapping("schemas")
    @LoginRequired
    public Object listSchemas(@RequestParam(value = "engine",defaultValue = "PRESTO") String engine) throws Exception {
        QueryObject queryObject = null;
        if (EngineType.valueOf(engine).equals(EngineType.PRESTO)) {
            queryObject = QueryObject.builder().engineType(EngineType.PRESTO).sql("show schemas").pageSize(DataCacheUtil.MaxSize).currentUser(ContextUtil.getCurrentUser().getName()).build();
        } else if (EngineType.valueOf(engine).equals(EngineType.HIVE)) {
            queryObject = QueryObject.builder().engineType(EngineType.HIVE).sql("show databases").pageSize(DataCacheUtil.MaxSize).currentUser(ContextUtil.getCurrentUser().getName()).build();
        }
        List<Map<String, Object>> mapList = queryService.getSchemas(queryObject).stream().map(schema -> {
            Map<String, Object> info = new HashMap<>();
            info.put("name", schema);
            info.put("leaf", false);
            return info;
        }).collect(Collectors.toList());
        return getResult(mapList);
    }

    @ResponseBody
    @GetMapping("tables")
    @LoginRequired
    public Object listTables(@RequestParam(value = "engine",defaultValue = "PRESTO") String engine, @RequestParam("database") String database, HttpServletRequest httpServletRequest) throws Exception {
        QueryObject queryObject = QueryObject.builder().engineType(EngineType.valueOf(engine)).sql(String.format("show tables from %s", database)).pageSize(DataCacheUtil.MaxSize).currentUser(ContextUtil.getCurrentUser().getName()).build();
        List<Map<String, Object>> mapList = queryService.getTables(queryObject).stream().map(schema -> {
            Map<String, Object> info = new HashMap<>();
            info.put("name", schema);
            info.put("database", database);
            info.put("leaf", false);
            return info;
        }).collect(Collectors.toList());
        return getResult(mapList);
    }

    @ResponseBody
    @GetMapping("table")
    @LoginRequired
    public Object showTableInfo(@RequestParam(value = "engine",defaultValue = "PRESTO") String engine,
                                @RequestParam("database") String database,
                                @RequestParam("table") String tableName) throws Exception {
        QueryObject queryObject = QueryObject.builder()
                .engineType(EngineType.valueOf(engine))
                .sql(String.format("desc %s.%s", database, tableName))
                .pageSize(DataCacheUtil.MaxSize)
                .currentUser(ContextUtil.getCurrentUser().getName()).build();
        Map<String, String> tableInfo = queryService.getTableInfo(queryObject);
        List<Map<String, Object>> mapList = new ArrayList<>();
        tableInfo.forEach((col,type)->{
            Map<String, Object> info = new HashMap<>();
            info.put("name", String.format("%s  %s", col,type));
            info.put("database", database);
            info.put("table", tableName);
            info.put("leaf", true);
            mapList.add(info);
        });
        return getResult(mapList);
    }

    @ResponseBody
    @GetMapping("sql")
    @LoginRequired
    public Object selectTableBySql(@RequestParam(value = "engine",defaultValue = "PRESTO") String engine, @RequestParam("sql") String sql,
                                   @RequestParam(value = "pageIndex", required = false, defaultValue = "1") int pageIndex,
                                   @RequestParam(value = "pageSize", required = false, defaultValue = "100") int pageSize)
            throws Exception {
        //todo 获取当前用户
        QueryObject queryObject = QueryObject.builder().currentUser(ContextUtil.getCurrentUser().getName()).pageSize(pageSize).engineType(EngineType.valueOf(engine)).sql(sql).build();
        //执行查询
        final long timeBegin = System.currentTimeMillis();
        Map<String, Object> executeQuery = queryService.executeQuery(queryObject, pageIndex);
        final long timeEnd = System.currentTimeMillis();
        Map<String, Object> result = getResult(executeQuery);
        result.put("timeUsed", (timeEnd - timeBegin) / 1000d);

        return result;
    }

    @ResponseBody
    @PostMapping("sql")
    @LoginRequired
    public Object saveSql(@RequestParam("name") String name, @RequestParam("sql") String sql)
            throws Exception {
        SavedSql savedSql = new SavedSql();
        savedSql.setCreator(ContextUtil.getCurrentUser().getName());
        savedSql.setName(name);
        savedSql.setSqlContent(sql);
        savedSql.setIsTrash(false);
        savedSql.setCreateTime(new Date());
        queryService.saveQuerySql(savedSql);
        return true;
    }


    @ResponseBody
    @GetMapping("sqls")
    @LoginRequired
    public Object getSaveSql()
            throws Exception {
        List<SavedSql> querySqlByCreator = queryService.getQuerySqlByCreator(ContextUtil.getCurrentUser().getName());
        return getResult(querySqlByCreator);
    }
}
