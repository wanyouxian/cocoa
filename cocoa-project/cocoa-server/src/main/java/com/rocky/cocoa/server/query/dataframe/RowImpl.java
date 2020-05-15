package com.rocky.cocoa.server.query.dataframe;

import java.util.*;
import java.util.function.Function;

class RowImpl implements Row {

    private Object[] values;
    private RowSchemaInfo schemaInfo;

    public RowImpl(RowSchemaInfo info) {
        this.schemaInfo = info;
        values = new Object[info.getColumns().size()];
    }

    @Override
    public void setValue(int index, Object value) {
        values[index] = value;
    }

    @Override
    public void setValue(String name, Object value) {
        values[schemaInfo.getColumnIndex(name)] = value;
    }

    @Override
    public int getInt(String name) {
        return Integer.parseInt(values[schemaInfo.getColumnIndex(name)].toString());
    }

    @Override
    public int getInt(int index) {
        return Integer.parseInt(values[index].toString());
    }

    @Override
    public double getDouble(String name) {
        return (double) values[schemaInfo.getColumnIndex(name)];
    }

    @Override
    public double getDouble(int index) {
        return (double) values[index];
    }

    @Override
    public boolean getBoolean(String name) {
        return (boolean) values[schemaInfo.getColumnIndex(name)];
    }

    @Override
    public boolean getBoolean(int index) {
        return (boolean) values[index];
    }

    @Override
    public Date getDate(String name) {
        return (Date) values[schemaInfo.getColumnIndex(name)];
    }

    @Override
    public Date getDate(int index) {
        return (Date) values[index];
    }

    @Override
    public String getString(String name) {
        return (String) values[schemaInfo.getColumnIndex(name)];

    }

    @Override
    public String getString(int index) {
        return (String) values[index];
    }

    @Override
    public Object getValueAs(String name) {
        return values[schemaInfo.getColumnIndex(name)];
    }

    @Override
    public Object getValueAs(int index) {
        return values[index];
    }

    @Override
    public int getColumnCount() {
        return this.schemaInfo.getColumns().size();
    }

    @Override
    public List<ColumnInfo> getColumnInfos() {
        return this.schemaInfo.getColumns();
    }

    @Override
    public ColumnInfo getColumnInfo(int index) {
        return this.schemaInfo.getColumnInfo(index);
    }

    @Override
    public <T> T apply(Function<Row, T> function) {
        return function.apply(this);
    }

    @Override
    public String toString() {
        StringBuilder sbu = new StringBuilder();
        for (Object v : values) {
            sbu.append(v != null ? v.toString() : null).append(",");
        }
        sbu.deleteCharAt(sbu.length() - 1);
        return sbu.toString();
    }

    @Override
    public Object[] getRowValues() {
        return values;
    }

    @Override
    public Object[] getRowValues(String[] names) {
        Object[] values = new Object[names.length];
        for (int i = 0; i < names.length; i++) {
            values[i] = this.getValueAs(names[i]);
        }
        return values;
    }


    @Override
    public void setRowValues(Object[] values) {
        this.values = values;
    }

    @Override
    public Map<String, Object> getRowMap() {
        Map<String, Object> rowMap = new HashMap<>();
        for (int i = 0; i < this.schemaInfo.getColumns().size(); i++) {
            rowMap.put(this.schemaInfo.getColumnName(i), this.values[i]);
        }
        return rowMap;
    }


}
