package com.rocky.cocoa.server.query.dataframe;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.serializers.CollectionSerializer;
import com.esotericsoftware.kryo.serializers.JavaSerializer;
import com.rocky.cocoa.server.query.DataCacheUtil;
import com.rocky.cocoa.server.query.RedisPool;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;

@Data
public class PagedDataFrame {

  private Logger logger = Logger.getLogger(PagedDataFrame.class);
  private RowSchemaInfo rowSchemaInfo;
  private int rowCount;
  private DataFrameImpl nowDataFrame;
  private int nowPageIndex = 1;
  private int nowPageSize = DataCacheUtil.PageSize;
  private volatile boolean loading = true;

  public PagedDataFrame() {
  }

  public PagedDataFrame(RowSchemaInfo info, DataFrameImpl dataFrame) {
    this.rowSchemaInfo = info;
    this.nowDataFrame = dataFrame;
  }

  /**
   * executeQuery.
   */
  public DataFrame executeQuery(String hexKey, int pageIndex, int pageSize) {
    Kryo kryo = new Kryo();
    kryo.register(Object[].class, new JavaSerializer());
    CollectionSerializer serializer1 = new CollectionSerializer();
    serializer1.setElementClass(Object[].class, new JavaSerializer());
    kryo.register(ArrayList.class, serializer1);
    DataFrameImpl dataFrame = new DataFrameImpl(rowSchemaInfo);
    int recordStart = pageIndex <= 1 ? 1 : (pageIndex - 1) * pageSize + 1;
    int recordStop = pageIndex * pageSize;
    int start = recordStart % DataCacheUtil.PageSize == 0 ?
            recordStart / DataCacheUtil.PageSize
        : recordStart / DataCacheUtil.PageSize + 1;
    long stop = recordStop % DataCacheUtil.PageSize == 0 ?
            recordStop / DataCacheUtil.PageSize
        : recordStop / DataCacheUtil.PageSize + 1;
    Jedis redisConn = null;
    try {
      redisConn = RedisPool.getInstance().getConnection();
      if (stop > redisConn.llen(hexKey.getBytes())) {
        while (isLoading()) {
          Thread.sleep(100);
        }
        stop = redisConn.llen(hexKey.getBytes());
      }

      List<byte[]> rangeLists = redisConn.lrange(hexKey.getBytes(),
              start - 1, stop - 1);
      int counter = (start - 1) * DataCacheUtil.PageSize + 1;
      for (byte[] row : rangeLists) {
        Input input = new Input(row);
        List<Object[]> values = kryo.readObject(input, ArrayList.class, serializer1);
        for (Object[] item : values) {
          if (counter >= recordStart && counter <= recordStop) {
            dataFrame.append(item);
          }
          if (counter > recordStop) {
            break;
          }
          counter++;
        }
      }
      redisConn.expire(hexKey.getBytes(), DataCacheUtil.expireTime);
    } catch (Exception e) {
      logger.error(ExceptionUtils.getStackTrace(e));
      e.printStackTrace();
    }
    return dataFrame;
  }
}
