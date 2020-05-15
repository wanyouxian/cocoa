package com.rocky.cocoa.server.query.cache;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.CollectionSerializer;
import com.esotericsoftware.kryo.serializers.JavaSerializer;
import com.rocky.cocoa.server.query.*;
import com.rocky.cocoa.server.query.dataframe.ColumnInfo;
import com.rocky.cocoa.server.query.dataframe.DataFrame;
import com.rocky.cocoa.server.query.dataframe.PagedDataFrame;
import com.rocky.cocoa.server.query.dataframe.Row;
import lombok.Data;
import redis.clients.jedis.Jedis;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Data
public class CacheThread implements Runnable {
    private ResultSet resultSet;
    private DataFrame dataFrame;
    private String hexKey;
    private Statement statement;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private PagedDataFrame pagedDataFrame;
    private QueryObject queryObject;


    @Override
    public void run() {
        Jedis conn = null;
        Kryo kryo = new Kryo();
        kryo.register(Object[].class, new JavaSerializer());
        CollectionSerializer serializer = new CollectionSerializer();
        serializer.setElementClass(Object[].class, new JavaSerializer());
        kryo.register(ArrayList.class, serializer);

        try {
            conn = RedisPool.getInstance().getConnection();
            Iterator<Row> iterator = dataFrame.iterator();
            List<Object[]> objects = new ArrayList<>();
            PagedDataFrame pagedDataFrame = DataFrameCache.getDataFrameLoadingCache().get(hexKey);
            int counter = 0;
            Output output = new Output(DataCacheUtil.bufferSize);
            conn.del(hexKey);
            while (iterator.hasNext()) {
                Row next = iterator.next();
                objects.add(next.getRowValues());
                counter++;
                if (counter % DataCacheUtil.PageSize == 0) {
                    kryo.writeObject(output, objects);
                    conn.rpush(hexKey.getBytes(), output.toBytes());

                    output.flush();
                    output.clear();
                    objects.clear();
                    pagedDataFrame.setRowCount(counter);
                }
            }

            List<ResultSetColumnReader> readers = new ArrayList<>(dataFrame.getRowSchemaInfo().getColumns().size());
            for (ColumnInfo columnInfo : dataFrame.getRowSchemaInfo().getColumns()) {
                readers.add(JdbcTypeMapping.getResultSetColumnReader(columnInfo.getType()));
            }


            while (resultSet.next() && counter <= DataCacheUtil.MaxSize) {
                List<Object> values = new ArrayList<>(readers.size());
                for (int i = 0; i < readers.size(); i++) {
                    Object value = readers.get(i).readValue(i + 1, resultSet);
                    values.add(value);
                }
                counter++;
                if (counter % DataCacheUtil.PageSize == 0) {
                    kryo.writeObject(output, objects);
                    conn.rpush(hexKey.getBytes(), output.toBytes());

                    output.flush();
                    output.clear();
                    objects.clear();
                    pagedDataFrame.setRowCount(counter);
                }
            }

            if (objects.size() > 0) {
                kryo.writeObject(output, objects);
                conn.rpush(hexKey.getBytes(), output.toBytes());

                output.flush();
                output.clear();
                objects.clear();
                pagedDataFrame.setRowCount(counter);
            }
            output.close();
            conn.expire(hexKey.getBytes(), DataCacheUtil.expireTime);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pagedDataFrame.setLoading(false);
            closeClosables(resultSet, connection, statement, preparedStatement);
        }
    }


    private void closeClosables(AutoCloseable... closeables) {
        for (AutoCloseable closeable : closeables) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
