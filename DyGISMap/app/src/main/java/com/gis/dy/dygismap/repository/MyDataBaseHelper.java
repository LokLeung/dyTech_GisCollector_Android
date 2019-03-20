package com.gis.dy.dygismap.repository;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyDataBaseHelper extends SQLiteOpenHelper{
    public static final String DB_NAME = "FeatureDataBase.db";
    public static final int DB_VERSION = 1;
    public static final String TABLE_NAME = "features";
    private SQLiteDatabase db;

    //创建 students 表的 sql 语句
    private static final String POINTS_CREATE_TABLE_SQL = "create table if not exists "
            + TABLE_NAME + "("
            + "id integer primary key autoincrement,"
            + "PointType varchar(10) not null,"
            + "point_id varchar(20) not null,"
            + "location_x varchar(25) not null,"
            + "location_y varchar(25) not null,"
            + "jingwushi varchar(25) not null,"
            + "cunjuwei varchar(25) not null,"
            + "jieluxiang varchar(25) not null,"
            + "menpaihao varchar(25) not null,"
            + "xiaoqumingchen varchar(25),"
            + "gongcangmingchen varchar(25),"
            + "xuexiaomingchen varchar(25),"
            + "yiyuanmingchen varchar(25),"
            + "jianzhuwumingchen varchar(25) not null,"
            + "loucengshu varchar(25) not null,"
            + "xiaoqudanyuan varchar(25),"
            + "fangjian varchar(25),"
            + "plateType varchar(25) not null,"
            + "comments varchar(25) not null,"
            + "time varchar(25) not null,"
            + "pic_name varchar(25) not null,"
            + "caijirenyuan varchar(25)"
            + ")";
    public MyDataBaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public Cursor exeSql(String sql) {
        db = this.getReadableDatabase();
        return db.rawQuery(sql, null);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // create table Orders(Id integer primary key, CustomName text, OrderPrice integer, Country text);
        sqLiteDatabase.execSQL(POINTS_CREATE_TABLE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
