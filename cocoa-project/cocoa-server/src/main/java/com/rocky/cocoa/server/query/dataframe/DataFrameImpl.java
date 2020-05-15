package com.rocky.cocoa.server.query.dataframe;

import java.io.OutputStream;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class DataFrameImpl implements DataFrame {

  protected RowSchemaInfo rowSchemaInfo;
  protected List<Row> rows = new ArrayList<>();
  protected DataFrameMetaData metaData = new InternalDataFrameMetaData();

  public DataFrameImpl(RowSchemaInfo schemaInfo) {
    this.rowSchemaInfo = schemaInfo;
  }

  public DataFrameImpl(RowSchemaInfo schemaInfo, List<Row> rows) {
    this.rowSchemaInfo = schemaInfo;
    rows.addAll(rows);
  }

  @Override
  public RowSchemaInfo getRowSchemaInfo() {
    return rowSchemaInfo;
  }

  @Override
  public Map<String, Object[]> transpose() {
    List<Column> columns = new ArrayList<>(rowSchemaInfo.getColumns().size());
    Map<String, Object[]> map = new HashMap<>();
    for (ColumnInfo column : this.rowSchemaInfo.getColumns()) {
      map.put(column.getName(), this.getColumn(column.getName()).getValues());
    }
    return map;
  }

  /**
   * aggregate columns.
   *
   * @param aggrColumns columns to be aggregated
   * @param fuctions aggretation function for each column
   * @param groupColumns group by columns
   * @return DataFrame df
   */
  @Override
  public DataFrame aggregate(String[] aggrColumns, AggregateFuction[] fuctions,
      String... groupColumns) {
    List<String> finalAggcols = new ArrayList<>();
    List<String> finalCols = new ArrayList<>();
    for (String col : aggrColumns) {
      for (AggregateFuction fuction : fuctions) {
        finalAggcols.add(fuction.shortName() + "_" + col);
      }
    }
    finalCols.addAll(finalAggcols);
    for (String col : groupColumns) {
      if (!finalCols.contains(col)) {
        finalCols.add(col);
      }
    }
    RowSchemaInfo.RowSchemaInfoBuilder schemaInfoBuilder = RowSchemaInfo.newSchemaBuilder();
    for (String col : finalAggcols) {
      schemaInfoBuilder.column(col, DataType.DOUBLE);
    }
    for (String col : finalAggcols.subList(finalAggcols.size(), finalAggcols.size())) {
      schemaInfoBuilder.column(col, metaData.getColumnType(col));
    }
    RowSchemaInfo schemaInfo = schemaInfoBuilder.build();
    DataFrame df = new DataFrameImpl(schemaInfo);
    Map<String, List<Row>> rowMap = new HashMap<>();
    for (Row row : this.rows) {
      StringBuffer sbu = new StringBuffer(",");
      for (String col : groupColumns) {
        sbu.append(row.getValueAs(col).toString());
      }
      String key = sbu.toString();
      List<Row> groupRows = rowMap.get(key);
      if (groupRows == null) {
        groupRows = new ArrayList<Row>();
        rowMap.put(key, groupRows);
      }
      groupRows.add(row);
    }

    for (List<Row> groupRows : rowMap.values()) {
      Double[][] columns = new Double[aggrColumns.length][];
      for (int i = 0; i < aggrColumns.length; i++) {
        Double[] d = new Double[groupRows.size()];
        int idx = 0;
        for (Row row : groupRows) {
          d[idx++] = ((Number) row.getValueAs(aggrColumns[i])).doubleValue();
        }
        columns[i] = d;
      }
      Object[] finalValues = new Object[finalCols.size()];
      int idx = 0;
      for (int i = 0; i < aggrColumns.length; i++) {
        for (int j = 0; j < fuctions.length; j++) {
          Double value = fuctions[j].apply(columns[i]);
          finalValues[idx++] = value;
        }
      }

      if (groupColumns.length > 0) {
        Row r = groupRows.get(0);
        for (Object v : r.getRowValues(groupColumns)) {
          finalValues[idx++] = v;
        }
      }
      df.append(finalValues);
    }

    return df;
  }

  /**
   * distinct by columns.
   *
   * @param columns distinct columns
   * @return DataFrame df
   */
  @Override
  public DataFrame distinct(String[] columns) {
    Set<String> distKeys = new HashSet<>();
    RowSchemaInfo.RowSchemaInfoBuilder schemaInfoBuilder = RowSchemaInfo.newSchemaBuilder();
    for (String col : columns) {
      schemaInfoBuilder.column(col, this.getMetaData().getColumnType(col));
    }
    RowSchemaInfo schemaInfo = schemaInfoBuilder.build();
    DataFrameImpl distFrame = new DataFrameImpl(schemaInfo);
    int[] indexes = new int[columns.length];
    for (int i = 0; i < columns.length; i++) {
      indexes[i] = this.metaData.getColumnIndex(columns[i]);
    }
    Iterator<Row> rowIterator = this.iterator();
    while (rowIterator.hasNext()) {
      Row row = rowIterator.next();
      Object[] values = row.getRowValues();
      Object[] dvalues = new Object[columns.length];
      StringBuffer sbu = new StringBuffer();
      for (int i = 0; i < columns.length; i++) {
        dvalues[i] = values[indexes[i]];
        sbu.append(dvalues[i].toString());
      }
      String key = sbu.toString();
      if (!distKeys.contains(key)) {
        distKeys.add(key);
        distFrame.append(dvalues);
      }
    }
    return distFrame;
  }

  @Override
  public DataFrameMetaData getMetaData() {

    return metaData;
  }

  @Override
  public DataFrame slice(int startRow, int endRow) {
    DataFrameImpl newDf = new DataFrameImpl(this.rowSchemaInfo);
    endRow = endRow > rows.size() ? rows.size() : endRow;
    newDf.rows.addAll(rows.subList(startRow, endRow));
    return newDf;
  }

  @Override
  public int rowCount() {
    return this.rows.size();
  }

  @Override
  public int columnCount() {
    return this.rowSchemaInfo.getColumns().size();
  }

  @Override
  public DataFrame selectColumns(String[] columns) {
    List<ColumnInfo> columnInfos = new ArrayList<>();
    for (String column : columns) {
      columnInfos.add(this.rowSchemaInfo.getColumnInfo(column));
    }
    RowSchemaInfo schemaInfo = new RowSchemaInfo(columnInfos);
    DataFrameImpl newDf = new DataFrameImpl(schemaInfo);
    ColumnSelectRowFunction selectRowFunction = new ColumnSelectRowFunction(schemaInfo);
    this.rows.stream().forEach(new Consumer<Row>() {
      @Override
      public void accept(Row row) {
        Row newRow = selectRowFunction.apply(row);
        newDf.rows.add(newRow);
      }
    });
    return newDf;
  }

  @Override
  public DataFrame removeColumns(String[] columns) {
    Set<String> names = new HashSet<>();
    for (String name : columns) {
      names.add(name);
    }
    List<String> leftColumns = new ArrayList<>();
    for (ColumnInfo columnInfo : this.rowSchemaInfo.getColumns()) {
      String name = columnInfo.getName();
      if (!names.contains(name)) {
        leftColumns.add(name);
      }
    }
    return selectColumns(leftColumns.toArray(new String[leftColumns.size()]));
  }

  @Override
  public List<Row> head(int count) {
    int start = 0;
    int end = count <= this.rowCount() ? count : this.rowCount();
    return rows.subList(start, end);
  }

  @Override
  public List<Row> tail(int count) {
    int end = rows.size();
    int start = rows.size() > count ? rows.size() - count : 0;
    return rows.subList(start, end);
  }

  @Override
  public DataFrame unionAll(List<DataFrame> others) {
    DataFrameImpl newFrame = new DataFrameImpl(this.rowSchemaInfo);
    newFrame.rows.addAll(this.rows);
    for (DataFrame df : others) {
      Iterator<Row> iterator = df.iterator();
      while (iterator.hasNext()) {
        newFrame.rows.add(iterator.next());
      }
    }
    return newFrame;
  }

  @Override
  public void append(List<Object> rowValues) {
    Row row = GenericaRowFactory.createRow(rowSchemaInfo, rowValues);
    this.rows.add(row);
  }

  @Override
  public void append(Object[] rowValues) {
    Row row = GenericaRowFactory.createRow(rowSchemaInfo, rowValues);
    this.rows.add(row);
  }

  @Override
  public void serializeTo(OutputStream outputStream, DataFrameSerializer serializer) {
    serializer.serialize(outputStream, this);
  }

  @Override
  public Column getColumn(String name) {
    return new InternalColumn(name);
  }

  @Override
  public Iterator<Row> iterator() {
    return this.rows.iterator();
  }


  private class InternalDataFrameMetaData implements DataFrameMetaData {

    @Override
    public int getColumnCount() {
      return rowSchemaInfo.getColumns().size();
    }

    @Override
    public List<ColumnInfo> getColumns() {
      return rowSchemaInfo.getColumns();
    }

    @Override
    public String getColumnName(int index) {
      return rowSchemaInfo.getColumnName(index);
    }

    @Override
    public int getColumnIndex(String name) {
      return rowSchemaInfo.getColumnIndex(name);
    }

    @Override
    public DataType getColumnType(int index) {
      return rowSchemaInfo.getColumnInfo(index).getType();
    }

    @Override
    public DataType getColumnType(String name) {
      return rowSchemaInfo.getDataType(name);
    }
  }

  private class InternalColumn implements Column {

    private String name;
    private int index;

    public InternalColumn(String columnName) {
      this.name = columnName;
      this.index = rowSchemaInfo.getColumnIndex(name);
    }

    @Override
    public DataType getDataType() {
      return rowSchemaInfo.getDataType(name);
    }

    @Override
    public <T> T apply(Function<Column, T> function) {
      return function.apply(this);
    }

    @Override
    public int rowCount() {
      return rows.size();
    }

    @Override
    public int getInt(int rowIndex) {
      return rows.get(rowIndex).getInt(index);
    }

    @Override
    public double getDouble(int rowIndex) {
      return rows.get(rowIndex).getDouble(index);
    }

    @Override
    public boolean getBoolean(int rowIndex) {
      return rows.get(rowIndex).getBoolean(index);
    }

    @Override
    public String getString(int rowIndex) {
      return rows.get(rowIndex).getString(name);
    }

    @Override
    public Date getDate(int rowIndex) {
      return rows.get(rowIndex).getDate(index);
    }

    @Override
    public Object getValueAs(int rowIndex) {
      return rows.get(rowIndex).getValueAs(index);
    }

    @Override
    public String getName() {
      return this.name;
    }

    @Override
    public Object[] getValues() {
      Object[] values = new Object[rows.size()];
      for (int i = 0; i < rows.size(); i++) {
        values[i] = this.getValueAs(i);
      }
      return values;
    }
  }

  private class ColumnSelectRowFunction implements Function<Row, Row> {

    private RowSchemaInfo destSchema;

    public ColumnSelectRowFunction(RowSchemaInfo destSchema) {
      this.destSchema = destSchema;
    }

    @Override
    public Row apply(Row row) {
      Row distRow = new RowImpl(destSchema);
      for (ColumnInfo columnInfo : destSchema.getColumns()) {
        String name = columnInfo.getName();
        distRow.setValue(name, row.getValueAs(name));
      }
      return distRow;
    }
  }
}


