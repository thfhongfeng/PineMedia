package com.pine.template.db_server.sqlite.server;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.pine.template.db_server.DbKeyConstants;
import com.pine.tool.util.LogUtils;

import org.json.JSONObject;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SQLiteBaseServer {
    protected static final String TAG = LogUtils.makeLogTag(SQLiteBaseServer.class);

    //定义生成随机字符串的函数
    public static String randomString(int length) {
        // 首先定义一个随机数生成器
        SecureRandom secureRandom = new SecureRandom();
        // 使用可打印的 ASCII 字符作为生成字符串的字符集合
        String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        // 用 StringBuilder 保存生成的字符串
        StringBuilder sb = new StringBuilder();
        // 循环生成指定长度的随机字符串
        for (int i = 0; i < length; i++) {
            // 从字符集合中随机选择一个字符
            int randomIndex = secureRandom.nextInt(characters.length());
            char randomChar = characters.charAt(randomIndex);
            // 将选择的字符添加到字符串中
            sb.append(randomChar);
        }
        // 返回生成的随机字符串
        return sb.toString();
    }

    public static long insert(@NonNull SQLiteDatabase db, @NonNull String tableName,
                              @NonNull String nullColumnHack,
                              @NonNull ContentValues values) throws SQLException {
        return db.insert(tableName, nullColumnHack, values);
    }

    public static long insert(@NonNull SQLiteDatabase db, @NonNull String tableName,
                              @NonNull JSONObject object) throws SQLException {
        if (object != null) {
            ContentValues cv = new ContentValues();
            Iterator<String> iterator = object.keys();
            String first = iterator.next();
            String nullColumnHack = first;
            cv.put(first, object.optString(first));
            while (iterator.hasNext()) {
                String key = iterator.next();
                cv.put(key, object.optString(key));
            }
            long id = db.insert(tableName, nullColumnHack, cv);
            return id;
        }
        return -1;
    }

    public static long insert(@NonNull SQLiteDatabase db, @NonNull String tableName,
                              @NonNull Map<String, String> params) throws SQLException {
        if (params != null && params.size() > 0) {
            ContentValues cv = new ContentValues();
            Iterator<Map.Entry<String, String>> iterator = params.entrySet().iterator();
            Map.Entry<String, String> first = iterator.next();
            String nullColumnHack = first.getKey();
            cv.put(first.getKey(), first.getValue());
            while (iterator.hasNext()) {
                Map.Entry<String, String> entry = iterator.next();
                cv.put(entry.getKey(), entry.getValue());
            }
            long id = db.insert(tableName, nullColumnHack, cv);
            return id;
        }
        return -1;
    }

    public static Cursor query(@NonNull SQLiteDatabase db, @NonNull String tableName,
                               String[] columns, String selection,
                               String[] selectionArgs, String groupBy, String having,
                               String orderBy) throws SQLException {
        return db.query(tableName,
                columns, selection, selectionArgs, groupBy, having, orderBy);
    }

    public static Cursor query(@NonNull SQLiteDatabase db, @NonNull String tableName,
                               Map<String, String> params) throws SQLException {
        String filter = "";
        List<String> filterArgs = null;
        int pageNo = -1;
        int pageSize = -1;
        if (params != null && params.size() > 0) {
            filterArgs = new ArrayList<>();
            Iterator<Map.Entry<String, String>> iterator = params.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String> entry = iterator.next();
                if (DbKeyConstants.PAGE_NO.equals(entry.getKey())) {
                    pageNo = Integer.parseInt(entry.getValue());
                } else if (DbKeyConstants.PAGE_SIZE.equals(entry.getKey())) {
                    pageSize = Integer.parseInt(entry.getValue());
                } else {
                    filter += " " + entry.getKey() + "=?" + " and";
                    filterArgs.add(entry.getValue() == null ? "" : entry.getValue());
                }
            }
        }
        String limit = "";
        if (pageSize > 0) {
            limit = " limit " + pageSize;
            if (pageNo > 1 && pageSize > 1) {
                limit += " offset " + pageSize * (pageNo - 1);
            }
        }
        String sql = "select * from " + tableName;
        if (!TextUtils.isEmpty(filter)) {
            filter = filter.substring(0, filter.length() - 4);
            sql += " where" + filter;
        }
        if (!TextUtils.isEmpty(limit)) {
            sql += "" + limit;
        }
        LogUtils.d(TAG, "sql : " + sql);
        Cursor cursor = db.rawQuery(sql, filterArgs == null ? null : filterArgs.toArray(new String[0]));
        return cursor;
    }

    public static Cursor dimSearch(@NonNull SQLiteDatabase db, @NonNull String tableName,
                                   Map<String, String> params) throws SQLException {
        String filter = "";
        int pageNo = -1;
        int pageSize = -1;
        if (params != null && params.size() > 0) {
            Iterator<Map.Entry<String, String>> iterator = params.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String> entry = iterator.next();
                if (DbKeyConstants.PAGE_NO.equals(entry.getKey())) {
                    pageNo = Integer.parseInt(entry.getValue());
                } else if (DbKeyConstants.PAGE_SIZE.equals(entry.getKey())) {
                    pageSize = Integer.parseInt(entry.getValue());
                } else {
                    if (!TextUtils.isEmpty(entry.getValue())) {
                        filter += " " + entry.getKey() + " like '%" + entry.getValue() + "%'" + " and";
                    }
                }
            }
        }
        String limit = "";
        if (pageSize > 0) {
            limit = " limit " + pageSize;
            if (pageNo > 1 && pageSize > 1) {
                limit += " offset " + pageSize * (pageNo - 1);
            }
        }
        String sql = "select * from " + tableName;
        if (!TextUtils.isEmpty(filter)) {
            filter = filter.substring(0, filter.length() - 4);
            sql += " where" + filter;
        }
        if (!TextUtils.isEmpty(limit)) {
            sql += "" + limit;
        }
        LogUtils.d(TAG, "sql : " + sql);
        Cursor cursor = db.rawQuery(sql, null);
        return cursor;
    }

    public static int update(@NonNull SQLiteDatabase db, @NonNull String tableName,
                             ContentValues values, String whereCause, String[] whereArgs) throws SQLException {
        return db.update(tableName, values, whereCause, whereArgs);
    }

    public static int update(@NonNull SQLiteDatabase db, @NonNull String tableName,
                             @NonNull String idKey, @NonNull String id,
                             Map<String, String> params) throws SQLException {
        if (params != null && params.size() > 0) {
            ContentValues cv = new ContentValues();
            Iterator<Map.Entry<String, String>> iterator = params.entrySet().iterator();
            Map.Entry<String, String> first = iterator.next();
            cv.put(first.getKey(), first.getValue());
            while (iterator.hasNext()) {
                Map.Entry<String, String> entry = iterator.next();
                cv.put(entry.getKey(), entry.getValue());
            }
            int count = db.update(tableName, cv, idKey + "=?", new String[]{id});
            return count;
        }
        return -1;
    }
}
