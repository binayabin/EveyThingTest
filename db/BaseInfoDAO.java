package com.frkj.pda.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by binyaya on 2015/8/9.
 */
public class BaseInfoDAO {

    private static BaseInfoDAO baseInfoDao;
    private BaseInfoDBOpenHelper dbHelper;

    private BaseInfoDAO(Context context) {
        dbHelper = new BaseInfoDBOpenHelper(context);
    }

    /**
     * 获取连接的数据库
     *
     * @return 数据库
     */
    private synchronized SQLiteDatabase getConnectionDB() {
        SQLiteDatabase sqLiteDatabase = null;
        try {
            sqLiteDatabase = dbHelper.getReadableDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sqLiteDatabase;
    }

    /**
     * 获取数据库实体
     *
     * @param context
     * @return
     */
    public synchronized static BaseInfoDAO getInstance(Context context) {
        if (baseInfoDao == null) {
            baseInfoDao = new BaseInfoDAO(context);
        }
        return baseInfoDao;
    }

    /**
     * 将一条唯一码信息保存到唯一码表
     *
     * @param sysuniqueno 唯一码实体值
     * @return 是否保存成功
     */
    public synchronized boolean saveUniqueNO(Sys_unique_no sysuniqueno) {
        boolean success = false;
        if (sysuniqueno != null) {
            SQLiteDatabase db = getConnectionDB();
            try {
                ContentValues values = new ContentValues();
                values.put("BAR_CODE", sysuniqueno.getBar_code());
                values.put("UNIQUE_NO", sysuniqueno.getUnique_no());
                values.put("IS_USED", sysuniqueno.getIs_used());
                db.insert(BaseInfoDBOpenHelper.TABLE_NAME_UNIQUE_NO, null, values);
                success = true;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (db != null) {
                    db.close();
                }
            }
        }
        return success;
    }

    /**
     * 根据唯一码值更新基础资料表
     *
     * @param unique_noSet 唯一码实体值集合
     * @return 是否保存成功
     */
    public synchronized boolean updateUniqueNOFlag(Set<String> unique_noSet) {
        boolean success = false;
        if (!unique_noSet.isEmpty()) {
            SQLiteDatabase db = getConnectionDB();
            try {
                db.beginTransaction();
                for (String unique_no : unique_noSet){
                    String updateSql = "UPDATE "+BaseInfoDBOpenHelper.TABLE_NAME_UNIQUE_NO+" SET IS_USED = 1  WHERE  UNIQUE_NO = '"+unique_no+"'";
                    db.execSQL(updateSql);
                }
                db.setTransactionSuccessful();
                success = true;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (db != null) {
                    db.endTransaction();
                    db.close();
                }
            }
        }
        return success;
    }

    /**
     * 将下载得到的json格式数据保存到数据库
     * @param jsonObject
     * @return
     */
    public synchronized boolean insertIntoDBTablefromJSONOBject(JSONObject jsonObject) throws JSONException, SQLException{
        boolean success = false;
        SQLiteDatabase db = getConnectionDB();
        try {
            db.beginTransaction();
            JSONArray unique_no_datas = jsonObject.getJSONArray("prd_unique_no");
            int uiqueCodesCount = unique_no_datas.length();
            for (int i = 0; i < uiqueCodesCount; i++) {
                JSONObject uniqueNO_json = unique_no_datas.getJSONObject(i);
                String insertSql = "insert into "+BaseInfoDBOpenHelper.TABLE_NAME_UNIQUE_NO+" (BAR_CODE,UNIQUE_NO) values" +
                        "('" + uniqueNO_json.getString("bar_code") +
                        "','" + uniqueNO_json.getString("unique_no") + "')";
                db.execSQL(insertSql);
            }
            db.setTransactionSuccessful();
        } catch (JSONException e) {
            e.printStackTrace();
            throw new JSONException(e.getMessage());
        }catch (SQLException e){
            e.printStackTrace();
            throw new SQLException(e.getMessage());
        } finally {
            if (db != null){
                db.endTransaction();
                db.close();
            }
        }
        return success;
    }

    /**
     * 根据唯一码值查询符合条件的唯一码信息
     *
     * @param UNIQUE_NO 唯一码值
     * @return 唯一码实体集合
     */
    public List<Sys_unique_no> findUniqueEntityByUniqueNO(String UNIQUE_NO) {
        List<Sys_unique_no> list = new ArrayList<Sys_unique_no>();
        SQLiteDatabase db = getConnectionDB();
        Cursor cursor = null;
        try {
            cursor = db.query(BaseInfoDBOpenHelper.TABLE_NAME_UNIQUE_NO, null, "UNIQUE_NO = ?", new String[]{UNIQUE_NO}, null, null, null);
            if (cursor.moveToFirst()) {
                do {
                    Sys_unique_no sysuniqueno = new Sys_unique_no();
                    sysuniqueno.setBar_code(cursor.getString(cursor.getColumnIndex("BAR_CODE")));
                    sysuniqueno.setUnique_no(cursor.getString(cursor.getColumnIndex("UNIQUE_NO")));
                    sysuniqueno.setIs_used(cursor.getInt(cursor.getColumnIndex("IS_USED")));
                    list.add(sysuniqueno);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != db) {
                db.close();
            }
            if (null != cursor) {
                cursor.close();
            }
        }
        return list;
    }

    public List<Sys_unique_no> findAllUniqueEntity() {
        List<Sys_unique_no> list = new ArrayList<Sys_unique_no>();
        SQLiteDatabase db = getConnectionDB();
        Cursor cursor = null;
        try {
            cursor = db.query(BaseInfoDBOpenHelper.TABLE_NAME_UNIQUE_NO, null, null, null, null, null, null);
            if (cursor.moveToFirst()) {
                do {
                    Sys_unique_no sysuniqueno = new Sys_unique_no();
                    sysuniqueno.setBar_code(cursor.getString(cursor.getColumnIndex("BAR_CODE")));
                    sysuniqueno.setUnique_no(cursor.getString(cursor.getColumnIndex("UNIQUE_NO")));
                    sysuniqueno.setIs_used(cursor.getInt(cursor.getColumnIndex("IS_USED")));
                    list.add(sysuniqueno);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != db) {
                db.close();
            }
            if (null != cursor) {
                cursor.close();
            }
        }
        return list;
    }

    /**
     * 清空唯一码表SYS_UNIQUE_NO
     *
     * @return 是否清空成功
     */
    public synchronized boolean cleanTableSYS_UNIQUE_NO() {
        boolean success = false;
        SQLiteDatabase db = getConnectionDB();
        try {
            db.execSQL(dbHelper.DROP_TABLE_SYS_UNIQUE_NO);
            db.execSQL(dbHelper.CREATE_TABLE_SYS_UNIQUE_NO);
            success = true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (null != db) {
                db.close();
            }
        }
        return success;
    }

}
