package io.indy.octodo.model;

import java.util.ArrayList;
import java.util.List;

public class TableBuilder {

    private String mTableName;
    private List<String> mCols;

    public TableBuilder(String tableName) {
        mTableName = tableName;
        mCols = new ArrayList<String>();
    }

    public TableBuilder addText(String name) {
        mCols.add(name + " text");
        return this;
    }
    public TableBuilder addText(String name, String params) {
        mCols.add(name + " text " + params);
        return this;
    }
    public TableBuilder addInteger(String name) {
        mCols.add(name + " integer");
        return this;
    }
    public TableBuilder addInteger(String name, String params) {
        mCols.add(name + " integer " + params);
        return this;
    }
    public TableBuilder addTimestamp(String name) {
        mCols.add(name + " timestamp");
        return this;
    }
    public TableBuilder addTimestamp(String name, String params) {
        mCols.add(name + " timestamp " + params);
        return this;
    }

    public String build() {
        String comp = "";
        for(String unit : mCols) {
            comp = comp + unit + ",";
        }
        comp = comp.substring(0, comp.length() - 1);
        return "create table " + mTableName + " (" + comp + ");";
    }
}
