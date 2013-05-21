
package io.indy.octodo.model;

import java.util.ArrayList;
import java.util.List;

// generates strings that will be passed into SQLite

public class SQLTableStatement {

    private String mTableName;

    private List<String> mCols;

    public SQLTableStatement(String tableName) {
        mTableName = tableName;
        mCols = new ArrayList<String>();
    }

    public SQLTableStatement addText(String name) {
        mCols.add(name + " text");
        return this;
    }

    public SQLTableStatement addText(String name, String params) {
        mCols.add(name + " text " + params);
        return this;
    }

    public SQLTableStatement addInteger(String name) {
        mCols.add(name + " integer");
        return this;
    }

    public SQLTableStatement addInteger(String name, String params) {
        mCols.add(name + " integer " + params);
        return this;
    }

    public SQLTableStatement addTimestamp(String name) {
        mCols.add(name + " timestamp");
        return this;
    }

    public SQLTableStatement addTimestamp(String name, String params) {
        mCols.add(name + " timestamp " + params);
        return this;
    }

    public String create() {
        String comp = "";
        for (String unit : mCols) {
            comp = comp + unit + ",";
        }
        comp = comp.substring(0, comp.length() - 1);
        return "create table " + mTableName + " (" + comp + ");";
    }

    public String drop() {
        return "drop table if exists " + mTableName;
    }
}
