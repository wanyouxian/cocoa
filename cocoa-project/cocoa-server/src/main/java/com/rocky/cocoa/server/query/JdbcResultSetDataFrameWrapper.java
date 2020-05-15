package com.rocky.cocoa.server.query;

import com.rocky.cocoa.core.exception.ErrorCodes;
import com.rocky.cocoa.core.exception.CocoaException;
import com.rocky.cocoa.server.query.cache.CacheThread;
import com.rocky.cocoa.server.query.cache.CacheThreadPool;
import com.rocky.cocoa.server.query.cache.DataFrameCache;
import com.rocky.cocoa.server.query.dataframe.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcResultSetDataFrameWrapper implements ResultSetDataFrameWrapper<ResultSet> {
    private Statement st = null;
    private PreparedStatement ps = null;
    private Connection connection = null;
    private QueryObject queryObject = null;

    public JdbcResultSetDataFrameWrapper setResources(QueryObject queryObject, Statement st, PreparedStatement ps, Connection connection) {
        this.st = st;
        this.ps = ps;
        this.connection = connection;
        this.queryObject = queryObject;
        return this;
    }

    @Override
    public DataFrame wrapData(ResultSet result) {
        try {
            ResultSetMetaData metaData = result.getMetaData();

            RowSchemaInfo.RowSchemaInfoBuilder rowSchemaInfoBuilder = RowSchemaInfo.newSchemaBuilder();
            int columnCount = metaData.getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnName(i);
                DataType dataType = JdbcTypeMapping.getDataType(JDBCType.valueOf(metaData.getColumnType(i)));
                rowSchemaInfoBuilder.column(columnName, dataType);
            }
            RowSchemaInfo rowSchemaInfo = rowSchemaInfoBuilder.build();
            DataFrameImpl dataFrame = new DataFrameImpl(rowSchemaInfo);
            fillDataFrame(result, rowSchemaInfo, dataFrame);
            return dataFrame;
        } catch (SQLException e) {
            throw new CocoaException("sql exception" + e.getMessage(), ErrorCodes.SYSTEM_EXCEPTION);
        }
    }

    private void fillDataFrame(ResultSet result, RowSchemaInfo rowSchemaInfo, DataFrameImpl dataFrame) throws SQLException {

        List<ResultSetColumnReader> readers = new ArrayList<>(rowSchemaInfo.getColumns().size());
        for (ColumnInfo columnInfo : rowSchemaInfo.getColumns()) {
            readers.add(JdbcTypeMapping.getResultSetColumnReader(columnInfo.getType()));
        }

        int num = 0;
        PagedDataFrame pagedDataFrame = new PagedDataFrame(rowSchemaInfo, dataFrame);
        pagedDataFrame.setNowPageSize(queryObject.getPageSize());

        while (result.next() && queryObject.getPageSize() > 0) {
            List<Object> values = new ArrayList<>(readers.size());
            for (int i = 0; i < readers.size(); i++) {
                Object value = readers.get(i).readValue(i + 1, result);
                values.add(value);
            }
            dataFrame.append(values);
            if (++num >= queryObject.getPageSize() + 1) {
                pagedDataFrame.setRowCount(num);
                break;
            }
        }


        if (pagedDataFrame.getRowCount() == 0) {
            pagedDataFrame.setRowCount(num);
        }
        if(result.next()) {
            //todo 获取存储到缓存当中
            String unionStr = DataCacheUtil
                    .getUnionStrWithPid(queryObject.getCurrentUser(), queryObject.getSql());
            String uniqueKey = DataCacheUtil.generateMd5(unionStr);
            DataFrameCache.getDataFrameLoadingCache().put(uniqueKey, pagedDataFrame);
            CacheThread cacheThread = new CacheThread();
            cacheThread.setDataFrame(dataFrame);
            cacheThread.setPagedDataFrame(pagedDataFrame);
            cacheThread.setHexKey(uniqueKey);
            cacheThread.setConnection(connection);
            cacheThread.setStatement(st);
            cacheThread.setPreparedStatement(ps);
            cacheThread.setResultSet(result);
            cacheThread.setQueryObject(queryObject);
            CacheThreadPool.getInstance().execJob(cacheThread);
        }
    }
}
