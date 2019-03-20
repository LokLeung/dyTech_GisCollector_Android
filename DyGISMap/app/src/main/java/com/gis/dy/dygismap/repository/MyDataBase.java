package com.gis.dy.dygismap.repository;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.gis.dy.dygismap.MainActivity;
import com.gis.dy.dygismap.R;
import com.gis.dy.dygismap.model.ExcelUtils;
import com.gis.dy.dygismap.model.MyGISPoint;
import com.gis.dy.dygismap.repository.room.MyRoomDataBase;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MyDataBase {
    private static File file;
    //private static final Context context;
//    private MyDataBaseHelper myDataBaseHelper;
//    private SQLiteDatabase db;
    private static String workPath = Environment.getExternalStorageDirectory().toString() + "/shps/work";

    private static String[] title = {
            "点类型",
            "内部编号",
            "LON",
            "LAT",
            "所属警务室",
            "村居委",
            "街路巷",
            "门牌号",
            "小区名称",
            "工厂名称",
            "学校名称",
            "医院名称",
            "商铺名",
            "建筑物名称",
            "楼牌号",
            "单位名",
            "楼层数",
            "小区单元",
            "房间",
            "门牌类型",
            "备注",
            "采集时间",
            "相片名称",
            "采集人"};
    private String fileName;


    public MyDataBase(Context context){
        //this.context = context;
        //myDataBaseHelper = new MyDataBaseHelper(context);
    }

    public void exportExcel(String tablename,Context context) {
//        file = new File(Environment.getExternalStorageDirectory() + "/Record");
//        makeDir(file);
        fileName = workPath +"/"+tablename+".xls";
        ExcelUtils excelUtils = new ExcelUtils();
        excelUtils.initExcel(fileName, title);
        excelUtils.writeObjListToExcel(getRecordData(context), fileName, context);
    }

    /**
     * 将数据集合 转化成ArrayList<ArrayList<String>>
     * @return
     */
    public ArrayList<ArrayList<String>> getRecordData(Context context) {
        ArrayList<ArrayList<String>> tablelists = new ArrayList<>();
        Cursor mCrusor = MyRoomDataBase.getInstance(context).gpointDao().getAll();
        while (mCrusor.moveToNext()) {
            ArrayList<String> beanList=new ArrayList<String>();
            beanList.add(mCrusor.getString(1));
            beanList.add(mCrusor.getString(2));
            beanList.add(mCrusor.getString(3));
            beanList.add(mCrusor.getString(4));
            beanList.add(mCrusor.getString(5));
            beanList.add(mCrusor.getString(6));
            beanList.add(mCrusor.getString(7));
            beanList.add(mCrusor.getString(8));
            beanList.add(mCrusor.getString(9));
            beanList.add(mCrusor.getString(10));
            beanList.add(mCrusor.getString(11));
            beanList.add(mCrusor.getString(12));
            beanList.add(mCrusor.getString(13));
            beanList.add(mCrusor.getString(14));
            beanList.add(mCrusor.getString(15));
            beanList.add(mCrusor.getString(16));
            beanList.add(mCrusor.getString(17));
            beanList.add(mCrusor.getString(18));
            beanList.add(mCrusor.getString(19));
            beanList.add(mCrusor.getString(20));
            beanList.add(mCrusor.getString(21));
            beanList.add(mCrusor.getString(22));
            beanList.add(mCrusor.getString(23));
            beanList.add(mCrusor.getString(24));
            tablelists.add(beanList);
        }
        mCrusor.close();
        return tablelists;
    }

    public static void makeDir(File dir) {
        if (!dir.getParentFile().exists()) {
            makeDir(dir.getParentFile());
        }
        dir.mkdir();
    }

}

