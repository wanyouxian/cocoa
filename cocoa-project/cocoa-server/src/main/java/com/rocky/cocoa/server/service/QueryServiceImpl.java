package com.rocky.cocoa.server.service;

import com.google.common.base.Joiner;
import com.rocky.cocoa.core.exception.CocoaException;
import com.rocky.cocoa.entity.query.SavedSql;
import com.rocky.cocoa.repository.query.SavedSqlRepository;
import com.rocky.cocoa.server.query.ConnectionPool;
import com.rocky.cocoa.server.query.DataCacheUtil;
import com.rocky.cocoa.server.query.JdbcDao;
import com.rocky.cocoa.server.query.QueryObject;
import com.rocky.cocoa.server.query.cache.DataFrameCache;
import com.rocky.cocoa.server.query.dataframe.ColumnInfo;
import com.rocky.cocoa.server.query.dataframe.DataFrame;
import com.rocky.cocoa.server.query.dataframe.PagedDataFrame;
import com.rocky.cocoa.server.query.dataframe.Row;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Component
@Slf4j
public class QueryServiceImpl implements QueryService {
    @Autowired
    SavedSqlRepository savedSqlRepository;


    @Override
    public Map<String, Object> executeQuery(QueryObject queryObject, int pageIndex) throws ExecutionException, InterruptedException {
        return getData(executeDataFrame(queryObject, pageIndex), false);
    }

    private Map<String, Object> getData(DataFrame executeDataFrame, boolean convert) {
        Map<String, Object> result = new HashMap<>();

        List<String> metaData = new ArrayList<>();
        for (ColumnInfo columnInfo : executeDataFrame.getMetaData().getColumns()) {
            metaData.add(columnInfo.getName());
        }

        result.put("schema", metaData);

        //如果convert为true  按列返回信息
        if (convert) {
            for (String colName : metaData) {
                result.put(colName, new ArrayList<Object>());
            }

            for (Row row : executeDataFrame) {
                for (String colName : metaData) {
                    ((List<Object>) result.get(colName)).add(row.getValueAs(colName));
                }
            }
        } else {
            //convert为false 按行返回信息
            List<Map<String,Object>> data = new ArrayList<>();
            for (Row row : executeDataFrame) {
                data.add(row.getRowMap());
            }
            result.put("rows", data);
        }

        return result;
    }

    @Override
    public DataFrame executeDataFrame(QueryObject queryObject, int pageIndex) throws ExecutionException, InterruptedException {
        //获取hexkey
        String hexkey = DataCacheUtil.generateMd5(DataCacheUtil.getUnionStr(queryObject.getCurrentUser(), queryObject.getSql()));

        //从缓存中查找是否有DataFrame
        PagedDataFrame pagedDataFrame = DataFrameCache.getDataFrameLoadingCache().get(hexkey);
        //缓存中如果没有需要执行查询
        DataFrame dataFrame = null;
        if (pagedDataFrame.getRowSchemaInfo() == null) {
            int retry = 0;
            while (retry < 3) {
                log.debug(queryObject.getSql());
                Connection connection = ConnectionPool.getConnection(Joiner.on("|").join(Arrays.asList(queryObject.getCurrentUser(), queryObject.getEngineType().name())));
                try {
                    dataFrame = JdbcDao.queryAsDataFrame(queryObject, connection);
                    break;
                } catch (CocoaException e) {
                    e.printStackTrace();
                    log.error(e.getErrorMessage());
                    retry += 1;
                    Thread.sleep(1000);
                }
            }
        } else {
            if(pagedDataFrame.getNowPageIndex()==pageIndex&&pagedDataFrame.getNowPageSize()==queryObject.getPageSize()){
                return (DataFrame) pagedDataFrame;
            }
            dataFrame = pagedDataFrame.executeQuery(hexkey, pageIndex, queryObject.getPageSize());
        }
        pagedDataFrame.setNowPageSize(queryObject.getPageSize());
        pagedDataFrame.setNowPageIndex(pageIndex);
        return dataFrame;
    }

    @Override
    public List<String> getSchemas(QueryObject queryObject) throws Exception {
        Connection connection = ConnectionPool.getConnection(
                Joiner.on("|").join(Arrays.asList(queryObject.getCurrentUser()
                        , queryObject.getEngineType().name())));
        ResultSet resultSet = JdbcDao.query(queryObject, connection);
        List<String> schemas = new ArrayList<>();
        while (resultSet.next()) {
            schemas.add(resultSet.getString(1));
        }
        return schemas;
    }

    @Override
    public List<String> getTables(QueryObject queryObject) throws ExecutionException, SQLException {
        Connection connection = ConnectionPool.getConnection(Joiner.on("|").join(Arrays.asList(queryObject.getCurrentUser(), queryObject.getEngineType().name())));
        ResultSet resultSet = JdbcDao.query(queryObject, connection);
        List<String> tables = new ArrayList<>();
        while (resultSet.next()) {
            tables.add(resultSet.getString(1));
        }
        return tables;
    }

    @Override
    public Map<String, String> getTableInfo(QueryObject queryObject) throws ExecutionException, SQLException {
        Connection connection = ConnectionPool.getConnection(Joiner.on("|").join(Arrays.asList(queryObject.getCurrentUser(), queryObject.getEngineType().name())));
        ResultSet resultSet = JdbcDao.query(queryObject, connection);
        Map<String, String> tableInfo = new HashMap<>();
        while (resultSet.next()) {
            tableInfo.put(resultSet.getString(1), resultSet.getString(2));
        }
        return tableInfo;
    }

    @Override
    public void saveQuerySql(SavedSql savedSql) {
        savedSqlRepository.save(savedSql);
    }

    @Override
    public List<SavedSql> getQuerySqlByCreator(String creator) {
        return savedSqlRepository.findByCreator(creator);
    }

}
