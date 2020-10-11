package com.gis.dy.dygismap;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.drawerlayout.widget.DrawerLayout;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureQueryResult;
import com.esri.arcgisruntime.data.QueryParameters;
import com.esri.arcgisruntime.data.ShapefileFeatureTable;
import com.esri.arcgisruntime.geometry.CoordinateFormatter;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.Callout;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.LocationDisplay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
//import com.gc.materialdesign.views.ButtonRectangle;
import com.gis.dy.dygismap.model.ExcelUtils;
import com.gis.dy.dygismap.model.FileUtils;
import com.gis.dy.dygismap.model.MyGISPoint;
import com.gis.dy.dygismap.modelview.MapViewModel;
import com.gis.dy.dygismap.repository.MyDataBase;
import com.gis.dy.dygismap.repository.room.CacheEntity;
import com.gis.dy.dygismap.repository.room.MyRoomDataBase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private String copyPath = Environment.getExternalStorageDirectory().toString() + "/shps";
    private String workPath = Environment.getExternalStorageDirectory().toString() + "/shps/work";

    //控件
    private Button btn_GPS;
    private MapView mMapView;
    private Spinner spn_pointType;
    private Spinner spn_plateType;
    private TextView tv_features;
    private TextView tv_pics;
    private TextView tv_dbCount;
    int picCount = 0;
    private Button btn_addpics;
    private Button btn_submit;

    //EditText


    // 抽屉菜单对象
    public DrawerLayout drawerLayout;
    private LinearLayout main_left_drawer_layout;
    private LinearLayout main_right_drawer_layout;

    //arcgis things
    private LocationDisplay locationDisplay;
    private GraphicsOverlay overlay;
    private Feature mselectedFeature;
    private boolean movingPoint = false;
    MyGISPoint tempGPoint = new MyGISPoint();
    Feature movingFeature;

    //db
    MyRoomDataBase mydb;

    //modelview
    MapViewModel mapViewModel;

    //flag
    boolean exitflag = false;
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //license
        //ArcGISRuntimeEnvironment.setLicense("runtimelite,1000,rud9104491475,none,S080TK8EL96FTK118032");
        mydb = MyRoomDataBase.getInstance(MainActivity.this);
        //photoPaths = new ArrayList<>();
        picCount = 0;
        initApp();

        //超级小后门
        SimpleDateFormat myDateFormat = new SimpleDateFormat("yyyyMMdd");// HH:mm:ss
        //获取当前时间
        Date date = new Date(System.currentTimeMillis());
        String time = myDateFormat.format(date);
        int int_time = Integer.parseInt(time);
        if(int_time > 20210430){
            int count = mydb.cacheDao().getCount();
            try{
                if(count == 1){
                    //if cache existed , update
                    List<CacheEntity> thecaches = mydb.cacheDao().getLastCache();
                    thecaches.get(0).setLast_tilemap("yousuck");
                    mydb.cacheDao().updateCache(thecaches.get(0));
                }else if(count == 0){
                    //if not existed , insert
                    CacheEntity cache = new CacheEntity();
                    cache.setLast_tilemap("yousuck");
                    mydb.cacheDao().insertCache(cache);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }else{
            int count = mydb.cacheDao().getCount();
            try{
                if(count == 1){
                    //if cache existed , update
                    List<CacheEntity> thecaches = mydb.cacheDao().getLastCache();
                    thecaches.get(0).setLast_tilemap("good");
                    mydb.cacheDao().updateCache(thecaches.get(0));
                }else if(count == 0){
                    //if not existed , insert
                    CacheEntity cache = new CacheEntity();
                    cache.setLast_tilemap("good");
                    mydb.cacheDao().insertCache(cache);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        try{
        // try to read the cache
            if(mydb.cacheDao().getCount() == 1) {
                EditText et_worker = findViewById(R.id.eT_workerID);
                List<CacheEntity> caches = mydb.cacheDao().getLastCache();
                et_worker.setText(caches.get(0).getLast_worker());

                if(caches.get(0).getLast_tilemap().equals("yousuck")){
                    Toast.makeText(this, "凭证已过期,请联系攻城狮！",Toast.LENGTH_LONG).show();
                    ActivityManager.getInstance().addActivity(this);
                    exitflag = true;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void initViews() {
        ArrayAdapter mAdapter = new TestArrayAdapter(MainActivity.this, getResources().getStringArray(R.array.pointType));
        spn_pointType = (Spinner) findViewById(R.id.spinner_pointType);
        spn_pointType.setAdapter(mAdapter);

        tv_features = findViewById(R.id.tv_features_count);
        tv_pics = findViewById(R.id.tv_pic_count);
        tv_dbCount = findViewById(R.id.tv_db_count);

        //init drawer
        drawerLayout = findViewById(R.id.main_drawer_layout);
        main_left_drawer_layout = findViewById(R.id.main_left_drawer);
        main_right_drawer_layout = findViewById(R.id.main_right_drawer);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);//关闭手势侧滑
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
            }
            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                mMapView.setClickable(false);
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                mMapView.setClickable(true);
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

        //init the form fields
        spn_pointType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                tempGPoint.pointType = getResources().getStringArray(R.array.pointType)[i];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        ArrayAdapter mAdapter1 = new TestArrayAdapter(MainActivity.this, getResources().getStringArray(R.array.plateType));
        spn_plateType = (Spinner) findViewById(R.id.spinner_plateType);
        spn_plateType.setAdapter(mAdapter1);
        spn_plateType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                tempGPoint.plateType = getResources().getStringArray(R.array.plateType)[i];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        com.rey.material.widget.EditText et_jws = findViewById(R.id.eT_jingwushi);
        et_jws.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                tempGPoint.jingwushi = editable.toString();
            }
        });

        com.rey.material.widget.EditText et_cjw = findViewById(R.id.eT_cunjuwei);
        et_cjw.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                tempGPoint.cunjuwei = editable.toString();
            }
        });

        com.rey.material.widget.EditText et_jlx = findViewById(R.id.eT_jieluxiang);
        et_jlx.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                tempGPoint.jieluxiang = editable.toString();
            }
        });

        com.rey.material.widget.EditText et_mph = findViewById(R.id.eT_menpaihao);
        et_mph.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                tempGPoint.menpaihao = editable.toString();
            }
        });

        com.rey.material.widget.EditText et_jzw = findViewById(R.id.eT_jianzuwu);
        et_jzw.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                tempGPoint.jianzhuwumingchen = editable.toString();
            }
        });

        com.rey.material.widget.EditText et_lph = findViewById(R.id.eT_loupaihao);
        et_lph.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                tempGPoint.loupaihao = editable.toString();
            }
        });

        com.rey.material.widget.EditText et_spm = findViewById(R.id.eT_shangpuName);
        et_spm.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                tempGPoint.shangpuName = editable.toString();
            }
        });

        com.rey.material.widget.EditText et_dwm = findViewById(R.id.eT_danweiming);
        et_dwm.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                tempGPoint.danweiming = editable.toString();
            }
        });

        com.rey.material.widget.EditText et_lcs = findViewById(R.id.eT_floors);
        et_lcs.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                tempGPoint.loucengshu = editable.toString();
            }
        });

        com.rey.material.widget.EditText et_comment = findViewById(R.id.eT_comments);
        et_comment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                tempGPoint.comments = editable.toString();
            }
        });

        com.rey.material.widget.EditText et_xqdy = findViewById(R.id.eT_xiaoqudanyuan);
        et_xqdy.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                tempGPoint.xiaoqudanyuan = editable.toString();
            }
        });

        com.rey.material.widget.EditText et_xxmingzi = findViewById(R.id.eT_xxmingzi);
        et_xxmingzi.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                //特殊字段
                if (tempGPoint.pointType.equals("小区")) {
                    tempGPoint.xiaoquName = editable.toString();
                } else if (tempGPoint.pointType.equals("工厂"))
                    tempGPoint.gongcangName = editable.toString();
                else if (tempGPoint.pointType.equals("学校"))
                    tempGPoint.xuexiaoName = editable.toString();
                else if (tempGPoint.pointType.equals("医院"))
                    tempGPoint.yiyuanName = editable.toString();
            }
        });

        com.rey.material.widget.EditText et_roomName = findViewById(R.id.eT_roomName);
        et_roomName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                tempGPoint.roomName = editable.toString();
            }
        });
    }

    private void initMap(){
        mMapView = findViewById(R.id.mapView);
        // create a new map to display in the map view with a streets basemap
        mMapView.setMap(new ArcGISMap(Basemap.createLightGrayCanvas()));
        //drawing layout
        overlay = new GraphicsOverlay();
        mMapView.getGraphicsOverlays().add(overlay);
        //display the location
        initDisplayLocation();
    }

    private void initApp() {
        //copy shp files
        FileUtils.getInstance(this).copyAssetsToSD("shps", "shps").setFileOperateCallback(new FileUtils.FileOperateCallback() {
            @Override
            public void onSuccess() {
                Log.v("CopyFile", "Sucess!");
            }

            @Override
            public void onFailed(String error) {
                Log.v("CopyFile", "Failed!");
            }
        });

        //init workDir
        File file = new File(workPath);
        if (!file.exists()) {
            //通过file的mkdirs()方法创建目录中包含却不存在的文件夹
            file.mkdirs();
        }

        initViews();
        mapViewModel = new MapViewModel(this);
        mapViewModel.getfeatureLayer().observe(this,featureLayer -> {
            mMapView.getMap().getOperationalLayers().add(featureLayer);
            setMapListener();
        });
        mapViewModel.getShapefileFeatureTable().observe(this,featureTable -> tv_features.setText("shp已采集点数：" + featureTable.getTotalFeatureCount()));
        mapViewModel.getMap().observe(this, map -> mMapView.setMap(map));
        mapViewModel.getpicCount().observe(this,picCount->{tv_pics.setText("已添加照片:"+picCount);});

        setBtn();
        initMap();

        //init db
        mydb.gpointDao().getCountLD().observe(this, count -> {
                tv_dbCount.setText("地图已采集点数: "+count);
        });

        EditText et_workId = findViewById(R.id.eT_workerID);
        et_workId.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                //try to save the cahce
                int count = mydb.cacheDao().getCount();
                try{
                    if(count == 1){
                        //if cache existed , update
                        List<CacheEntity> thecaches = mydb.cacheDao().getLastCache();
                        thecaches.get(0).setLast_worker(et_workId.getText().toString());
                        //thecaches.get(0).setLast_tilemap(mapViewModel.tileCachePath);
                        mydb.cacheDao().updateCache(thecaches.get(0));
                    }else if(count == 0){
                        //if not existed , insert
                        CacheEntity cache = new CacheEntity();
                        cache.setLast_worker(et_workId.getText().toString());
                        //cache.setLast_tilemap(mapViewModel.tileCachePath);
                        mydb.cacheDao().insertCache(cache);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    public void initDisplayLocation() {
        locationDisplay = mMapView.getLocationDisplay();
        // Listen to changes in the status of the location data source.
        int requestCode = 2;
        String[] reqPermissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission
                .ACCESS_COARSE_LOCATION};
        locationDisplay.addDataSourceStatusChangedListener(dataSourceStatusChangedEvent -> {
            // If LocationDisplay started OK, then continue.
            if (dataSourceStatusChangedEvent.isStarted())
                return;

            // No error is reported, then continue.
            if (dataSourceStatusChangedEvent.getError() == null)
                return;

            // If an error is found, handle the failure to start.
            // Check permissions to see if failure may be due to lack of permissions.
            boolean permissionCheck1 = ContextCompat.checkSelfPermission(MainActivity.this, reqPermissions[0]) ==
                    PackageManager.PERMISSION_GRANTED;
            boolean permissionCheck2 = ContextCompat.checkSelfPermission(MainActivity.this, reqPermissions[1]) ==
                    PackageManager.PERMISSION_GRANTED;

            if (!(permissionCheck1 && permissionCheck2)) {
                // If permissions are not already granted, request permission from the user.
                ActivityCompat.requestPermissions(MainActivity.this, reqPermissions, requestCode);
            } else {
                // Report other unknown failure types to the user - for example, location services may not
                // be enabled on the device.
                String message = String.format("Error in DataSourceStatusChangedListener: %s", dataSourceStatusChangedEvent
                        .getSource().getLocationDataSource().getError().getMessage());
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();

                // Update UI to reflect that the location display did not actually start
                btn_GPS.setBackgroundColor(Color.GRAY);
                btn_GPS.setClickable(false);
            }
        });
    }

    public void setBtn() {
        //查看总点数
        Button btn_queryRooms = findViewById(R.id.btn_queryRooms);
        btn_queryRooms.setOnClickListener(view -> {
            try{
                List<MyGISPoint> roompoints = mydb.gpointDao().getAll2List();
                int count = 0;
                if(roompoints != null){
                    for(MyGISPoint roomPoint:roompoints){
                        if(roomPoint.roomName.contains("-")){
                            String[] roomNum = roomPoint.roomName.split("-");
                            //get floorStart and floorEnd
                            int floorStart = Integer.parseInt(roomNum[0].split("0")[0]);
                            int lenth = roomNum[1].length();
                            int floorEnd = Integer.parseInt(roomNum[1].substring(0,lenth-2));
                            int room = Integer.parseInt(roomNum[1].substring(lenth-2,lenth));
                            if(floorStart == floorEnd){
                                count+= room * (Integer.parseInt(roomPoint.loucengshu) - floorStart + 1);
                            }else
                                count += (floorEnd-floorStart+1) * room;
                        }
                        count+=1;
                    }
                }
                Toast.makeText(this, "已采总点数(含房间)："+count, Toast.LENGTH_SHORT).show();

            }catch (Exception e){
                Toast.makeText(this, "数据有错:"+e.toString(),Toast.LENGTH_SHORT).show();
            }
        });

        //导出表格
        Button btn_bind_field = findViewById(R.id.btn_bind_field);
        btn_bind_field.setOnClickListener(view -> {
            EditText et = findViewById(R.id.et_exlname);
            String filename = et.getText().toString();
            if(filename == "")
                Toast.makeText(this,"excel名字不能为空",Toast.LENGTH_SHORT).show();
            else {
                MyDataBase dataBase = new MyDataBase(this);
                dataBase.exportExcel(filename,MainActivity.this);
            }
        });

        //清空数据库按钮
        Button btn_clr_db = findViewById(R.id.btn_clr_db);
        btn_clr_db.setOnClickListener(view -> {
            AlertDialog.Builder clearDialog = new AlertDialog.Builder(MainActivity.this)
                    .setTitle("你确定要清空吗？")
                    .setPositiveButton("确定", (dialogInterface, i) -> {
                        AsyncTask.execute(() -> {
                            ShapefileFeatureTable shapefileFeatureTable = mapViewModel.getMapModel().getShapefileFeatureTable();
                            if(shapefileFeatureTable != null) {
                                QueryParameters query = new QueryParameters();
                                query.setWhereClause("PointType <> ' '");
                                // request all available attribute fields
                                final ListenableFuture<FeatureQueryResult> future = shapefileFeatureTable
                                        .queryFeaturesAsync(query);
                                // add done loading listener to fire when the selection returns
                                future.addDoneListener(() -> {
                                    try {
                                        //call get on the future to get the result
                                        FeatureQueryResult result = future.get();
                                        // create an Iterator
                                        Iterator<Feature> iterable = result.iterator();
                                        while(iterable.hasNext()){
                                            shapefileFeatureTable.deleteFeatureAsync(iterable.next());
                                        }
                                    }catch (Exception e){
                                        Log.e("clear shp",e.toString());
                                    }
                                });
                            }
                            mydb.gpointDao().clearTable();
                        });
                        dialogInterface.dismiss();
                    })
                    .setNegativeButton("取消", (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                    });
            clearDialog.show();
        });

        //拍照按钮
        btn_addpics = findViewById(R.id.btn_addPic);
        btn_addpics.setOnClickListener(view -> {
            dispatchTakePictureIntent(tempGPoint);
        });

        //定位按钮
        btn_GPS = findViewById(R.id.btn_GPS);
        btn_GPS.setOnClickListener(view -> {
            String tag = "btnGPS";

            if (!locationDisplay.isStarted()) {
                Log.v(tag, "start");
                btn_GPS.setBackgroundColor(Color.RED);
                locationDisplay.setShowLocation(true);
                locationDisplay.setAutoPanMode(LocationDisplay.AutoPanMode.NAVIGATION);
                locationDisplay.setShowPingAnimation(true);
                locationDisplay.startAsync();

            } else {
                Log.v(tag, "stop");
                btn_GPS.setBackgroundColor(Color.BLUE);
                locationDisplay.stop();
            }
        });

        //加载表格
        Button btn_load_table = findViewById(R.id.btn_load_table);
        btn_load_table.setOnClickListener(view -> {
            // define permission to request
            String[] reqPermission = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
            int requestCode = 2;
            // For API level 23+ request permission at runtime
            if (ContextCompat.checkSelfPermission(MainActivity.this,
                    reqPermission[0]) == PackageManager.PERMISSION_GRANTED) {
                Log.v("readFIle", "permission");
                //get files
                File map_detry = new File(workPath);
                Log.v("Files", map_detry.exists() + "");
                Log.v("Files", map_detry.isDirectory() + "");
                Log.v("Files", map_detry.listFiles() + "");
                File[] xls_files = map_detry.listFiles(path -> {
                    if (path.exists()) {
                        if (path.isDirectory() && path.canRead() && path.canWrite()) {
                            Log.i("readFile", "isDir");
                            return path.listFiles().length > 0;
                        }
                        if (path.isFile() && path.canRead() && path.canWrite()) {
                            if (path.getName().toLowerCase().endsWith(".xls")) {
                                return true;
                            }
                        }
                    }
                    return false;
                });
                if (xls_files == null) {
                    Toast.makeText(MainActivity.this, "读取表格文件失败", Toast.LENGTH_LONG).show();
                    return;
                }
                ArrayList<String> al_items = new ArrayList<>();
                for (File xls_file : xls_files) {
                    String filename = xls_file.getName();
                    if (filename.endsWith(".xls"))
                        al_items.add(filename);
                }
                String[] filenames = al_items.toArray(new String[0]);
                AlertDialog.Builder listDialog_files = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("选择要加载的表格")
                        .setItems(filenames, (dialogInterface1, which) -> {
                            try {
                                ExcelUtils eu = new ExcelUtils();
                                MyGISPoint[] gpoints = eu.getXlsData(workPath+ "/" +filenames[which]);
                                boolean flag = true;
                                for(MyGISPoint gpoint : gpoints){
                                    //if existed, warn
                                    if(mydb.gpointDao().findByLatLnt(gpoint.LAT,gpoint.LNG) != null){
                                        Toast.makeText(MainActivity.this,"不能加载重复表格！（检测到重复数据）",Toast.LENGTH_SHORT).show();
                                        flag = false;
                                        break;
                                    }
                                }
                                if(flag){
                                    AsyncTask.execute(()->{
                                        mydb.gpointDao().insertPoint(gpoints);
                                        if(mapViewModel.getMapModel().getShapefileFeatureTable() != null)
                                            mapViewModel.initSHP();
                                    });
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        });
                listDialog_files.show();
            } else {
                // request permission
                ActivityCompat.requestPermissions(MainActivity.this, reqPermission, requestCode);
            }
        });

        //test btn
        Button btn_test = findViewById(R.id.btn_Testdb);
        btn_test.setOnClickListener(view -> {
            try{
                ExifInterface exifInterface = new ExifInterface(mCurrentPhotoPath);
                String lng = exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
                Toast.makeText(MainActivity.this,lng,Toast.LENGTH_SHORT).show();
            }catch (Exception ex){
                ex.printStackTrace();
            }
        });

        //submit btn
        btn_submit = findViewById(R.id.btn_summit);
        btn_submit.setOnClickListener(view -> {
            if (picCount < 1) {
                Toast.makeText(this, "请至少拍一张照片！", Toast.LENGTH_SHORT).show();
            }

            if(drawerLayout.isDrawerOpen(main_right_drawer_layout)){
                //单个点
                AsyncTask.execute(()->{
                    mydb.gpointDao().insertPoint(tempGPoint);
                });
                mapViewModel.addPoint2SHP(tempGPoint);
                overlay.getGraphics().clear();
                picCount=0;
                mapViewModel.getpicCount().setValue(picCount);
                //makeEXIF();
                drawerLayout.closeDrawer(main_right_drawer_layout);
            }
        });

        Button btn_func = findViewById(R.id.btn_funcs);
        btn_func.setOnClickListener(view -> {
            drawerLayout.openDrawer(main_left_drawer_layout);
        });

        //load btn
        Button btn_load_shp = findViewById(R.id.btn_load_shp);
        btn_load_shp.setOnClickListener((View view) -> {

            if(exitflag){
                ActivityManager.getInstance().exit();
            }
            System.out.println("打开底图！");
            // define permission to request
            String[] reqPermission = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
            int requestCode = 2;
            // For API level 23+ request permission at runtime
            if (ContextCompat.checkSelfPermission(MainActivity.this,
                    reqPermission[0]) == PackageManager.PERMISSION_GRANTED) {
                Log.v("readFIle", "permission");
                //get files
                File map_detry = new File(copyPath);
                Log.v("Files", map_detry.exists() + "exitsts");
                Log.v("Files", map_detry.isDirectory() + "isDirectory");
                Log.v("Files", Arrays.toString(map_detry.listFiles()) + "");
                File[] map_files = map_detry.listFiles(path -> {
                    if (path.exists()) {
                        if (path.isDirectory() && path.canRead() && path.canWrite()) {
                            Log.i("readFile", "isDir");
                            return path.listFiles().length > 0;
                        }
                        if (path.isFile() && path.canRead() && path.canWrite()) {
                            if (path.getName().toLowerCase().endsWith(".tpk")) {
                                return true;
                            }
                        }
                    }
                    return false;
                });
                if (map_files == null) {
                    Toast.makeText(MainActivity.this, "读取地图文件失败", Toast.LENGTH_LONG).show();
                    return;
                }
                ArrayList<String> al_items = new ArrayList<>();
                for (File shp_file : map_files) {
                    String filename = shp_file.getName();
                    if (filename.endsWith(".tpk"))
                        al_items.add(filename);
                }
                String[] filenames = al_items.toArray(new String[0]);
                AlertDialog.Builder listDialog_files = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("选择要加载的底图")
                        .setItems(filenames, (dialogInterface1, which) -> {
                            mapViewModel.requestReadPermission(copyPath + "/" + filenames[which], 0);
                            mapViewModel.requestReadPermission(copyPath + "/" + "Standard_address.shp", 1);//加载shp
                            //try to save the cahce
//                            int count = mydb.cacheDao().getCount();
//                            try{
//                                if(count == 1){
//                                    //if cache existed , update
//                                    List<CacheEntity> thecaches = mydb.cacheDao().getLastCache();
//                                    thecaches.get(0).setLast_tilemap(mapViewModel.tileCachePath);
//                                    mydb.cacheDao().updateCache(thecaches.get(0));
//                                }else if(count == 0){
//                                    //if not existed , insert
//                                    CacheEntity cache = new CacheEntity();
//                                    cache.setLast_tilemap(mapViewModel.tileCachePath);
//                                    mydb.cacheDao().insertCache(cache);
//                                }
//                            }catch (Exception e){
//                                e.printStackTrace();
//                            }
                        });
                listDialog_files.show();
            } else {
                // request permission
                ActivityCompat.requestPermissions(MainActivity.this, reqPermission, requestCode);
            }
        });
    }

    //为地图view添加点击事件
    @SuppressLint("ClickableViewAccessibility")
    public void setMapListener() {
        ArcGISMap map = mMapView.getMap();
        ShapefileFeatureTable shapefileFeatureTable = mapViewModel.getShapefileFeatureTable().getValue();
        mMapView.setOnTouchListener(new DefaultMapViewOnTouchListener(this, mMapView) {
            //轻单击查询并显示点数据
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                Callout mCallout = mMapView.getCallout();
                // remove any existing callouts
                if (mCallout.isShowing()) {
                    mCallout.dismiss();
                }
                if(movingPoint && movingFeature!=null){
                    //move the point
                    final Point clickPoint = mMapView
                            .screenToLocation(new android.graphics.Point(Math.round(e.getX()), Math.round(e.getY())));
                    try {
                        String[] lat_lng = point2LATLNG(clickPoint);
                        String[] old_latlng = point2LATLNG(movingFeature.getGeometry().getExtent().getCenter());
                        AsyncTask.execute(() -> {
                            MyGISPoint tempPoints = mydb.gpointDao().findByLatLnt(old_latlng[0],old_latlng[1]);
                            tempPoints.LAT = lat_lng[0];
                            tempPoints.LNG = lat_lng[1];
                            mydb.gpointDao().updatePoint(tempPoints);
                        });
                        Point testPoint = new Point(Double.valueOf(lat_lng[1]),Double.valueOf(lat_lng[0]), SpatialReference.create(4326));
                        movingFeature.setGeometry(testPoint);
                        ListenableFuture<Void> updateFeatureAsync = shapefileFeatureTable.updateFeatureAsync(movingFeature);
                        updateFeatureAsync.addDoneListener(()-> {
                            mapViewModel.resetLabel();
                            movingFeature = null;
                        });
                    } catch (Exception ex) {
                        Log.e("moving failed",ex.toString());
                    }
                    movingPoint=false;
                }else {
                    // get the point that was clicked and convert it to a point in map coordinates
                    final Point clickPoint = mMapView
                            .screenToLocation(new android.graphics.Point(Math.round(e.getX()), Math.round(e.getY())));
                    // create a selection tolerance
                    int tolerance = 8;
                    double mapTolerance = tolerance * mMapView.getUnitsPerDensityIndependentPixel();
                    // use tolerance to create an envelope to query
                    Envelope envelope = new Envelope(clickPoint.getX() - mapTolerance, clickPoint.getY() - mapTolerance,
                            clickPoint.getX() + mapTolerance, clickPoint.getY() + mapTolerance, map.getSpatialReference());
                    QueryParameters query = new QueryParameters();
                    query.setGeometry(envelope);
                    // request all available attribute fields
                    final ListenableFuture<FeatureQueryResult> future = shapefileFeatureTable
                            .queryFeaturesAsync(query);
                    // add done loading listener to fire when the selection returns
                    future.addDoneListener(() -> {
                        try {
                            //call get on the future to get the result
                            FeatureQueryResult result = future.get();
                            // create an Iterator
                            Iterator<Feature> iterator = result.iterator();

                            // cycle through selections
                            Feature feature;
                            if (iterator.hasNext()) {
                                feature = iterator.next();
                                movingFeature = feature;
                                // create a Map of all available attributes as name value pairs
                                Point location = feature.getGeometry().getExtent().getCenter();
                                String[] location_latlng = point2LATLNG(location);
                                MyGISPoint queryPoint = mydb.gpointDao().findByLatLnt(location_latlng[0],location_latlng[1]);

                                int rooms = 0;
                                if(queryPoint.roomName.contains("-")){
                                    String[] roomNum = queryPoint.roomName.split("-");
                                    //get floorStart and floorEnd
                                    int floorStart = Integer.parseInt(roomNum[0].split("0")[0]);
                                    int lenth = roomNum[1].length();
                                    int floorEnd = Integer.parseInt(roomNum[1].substring(0,lenth-2));
                                    int room = Integer.parseInt(roomNum[1].substring(lenth-2,lenth));
                                    if(floorStart == floorEnd){
                                        rooms+= room * (Integer.parseInt(queryPoint.loucengshu) - floorStart+1);
                                    }else
                                        rooms += (floorEnd-floorStart+1) * room;
                                }

                                // create a TextView to display field values
                                TextView calloutContent = new TextView(getApplicationContext());
                                calloutContent.setTextColor(Color.BLACK);
                                calloutContent.setSingleLine(false);
                                calloutContent.setVerticalScrollBarEnabled(true);
                                calloutContent.setScrollBarStyle(View.SCROLLBARS_INSIDE_INSET);
                                calloutContent.setMovementMethod(new ScrollingMovementMethod());
                                calloutContent.setLines(13);
                                // append name value pairs to TextView
                                calloutContent.append("点类型" + " | " + queryPoint.pointType + "\n");
                                calloutContent.append("门牌类型" + " | " + queryPoint.plateType + "\n");
                                calloutContent.append("街路巷" + " | " + queryPoint.jieluxiang + "\n");
                                calloutContent.append("门牌号" + " | " + queryPoint.menpaihao + "\n");
                                calloutContent.append("村居委" + " | " + queryPoint.cunjuwei + "\n");
                                calloutContent.append("警务室" + " | " + queryPoint.jingwushi + "\n");
                                calloutContent.append("楼牌号" + " | " + queryPoint.loupaihao + "\n");
                                calloutContent.append("楼层数" + " | " + queryPoint.loucengshu + "\n");
                                calloutContent.append("房间数" + " | " + rooms + "\n");
                                calloutContent.append("房间名" + " | " +  queryPoint.roomName+ "\n");
                                if(queryPoint.pointType.equals("商铺"))
                                    calloutContent.append("商铺名" + " | " +  queryPoint.shangpuName+ "\n");
                                calloutContent.append("采集时间" + " | " + queryPoint.time + "\n");
                                calloutContent.append("采集人" + " | " + queryPoint.workerID + "\n");
                                calloutContent.append("经纬度" + " | " + queryPoint.LAT + ","+queryPoint.LNG + "\n");

                                // center the mapview on selected feature
                                Envelope envelope1 = feature.getGeometry().getExtent();
                                mMapView.setViewpointGeometryAsync(envelope1, 200);

                                //set callout onclick
                                LinearLayout calloutLayout = new LinearLayout(getApplicationContext());

                                ImageView imageView = new ImageView(getApplicationContext());
                                imageView.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_mode_edit_black_48dp));
                                ImageView imageView_move = new ImageView(getApplicationContext());
                                imageView_move.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_shuffle_black_48dp));
                                imageView.setPadding(5,0,5,0);
                                ImageView imageView_imgs = new ImageView(getApplicationContext());
                                imageView_imgs.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_image_black_48dp));
                                imageView_imgs.setPadding(5,0,5,0);
                                ImageView imageView_delete = new ImageView(getApplicationContext());
                                imageView_delete.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_delete_forever_black_48dp));
                                imageView_delete.setPadding(5,0,5,0);
                                mselectedFeature = feature;

                                //set imgview event
                                imageView.setOnClickListener(view -> {
                                    View dialog_view = View.inflate(MainActivity.this, R.layout.modify_dialog, null);
                                    Spinner et_pt = dialog_view.findViewById(R.id.spinner_pointType);
                                    String[] items = getResources().getStringArray(R.array.pointType);
                                    ArrayAdapter mAdapter = new TestArrayAdapter(MainActivity.this, items);
                                    et_pt.setAdapter(mAdapter);
                                    int seletion = 0;
                                    for(String item : items){
                                        Log.e("item",item);
                                        Log.e("pointtype",queryPoint.pointType);
                                        if(item.equals( queryPoint.pointType)){
                                            break;
                                        }
                                        seletion++;
                                    }
                                    et_pt.setSelection(seletion);
                                    com.rey.material.widget.EditText et_jws = dialog_view.findViewById(R.id.eT_jingwushi);
                                    et_jws.setText(queryPoint.jingwushi);
                                    com.rey.material.widget.EditText et_cjw = dialog_view.findViewById(R.id.eT_cunjuwei);
                                    et_cjw.setText(queryPoint.cunjuwei);
                                    com.rey.material.widget.EditText et_jlx = dialog_view.findViewById(R.id.eT_jieluxiang);
                                    et_jlx.setText(queryPoint.jieluxiang);
                                    com.rey.material.widget.EditText et_mph = dialog_view.findViewById(R.id.eT_menpaihao);
                                    et_mph.setText(queryPoint.menpaihao);
                                    com.rey.material.widget.EditText et_jzw = dialog_view.findViewById(R.id.eT_jianzuwu);
                                    et_jzw.setText(queryPoint.jianzhuwumingchen);
                                    com.rey.material.widget.EditText et_lph = dialog_view.findViewById(R.id.eT_loupaihao);
                                    et_lph.setText(queryPoint.loupaihao);
                                    com.rey.material.widget.EditText et_danweiming = dialog_view.findViewById(R.id.eT_danweiming);
                                    et_danweiming.setText(queryPoint.danweiming);
                                    com.rey.material.widget.EditText et_lcs = dialog_view.findViewById(R.id.eT_floors);
                                    et_lcs.setText(queryPoint.loucengshu);
                                    com.rey.material.widget.EditText eT_rooms = dialog_view.findViewById(R.id.eT_rooms);
                                    eT_rooms.setText(queryPoint.roomName);
                                    com.rey.material.widget.EditText et_xqdy = dialog_view.findViewById(R.id.eT_xiaoqudanyuan);
                                    et_xqdy.setText(queryPoint.xiaoqudanyuan);
                                    com.rey.material.widget.EditText et_xxmz = dialog_view.findViewById(R.id.eT_xxmingzi);
                                    switch (queryPoint.pointType){
                                        case "小区":
                                            et_xxmz.setText(queryPoint.xiaoquName);
                                            break;
                                        case "医院":
                                            et_xxmz.setText(queryPoint.yiyuanName);
                                            break;
                                        case "工厂":
                                            et_xxmz.setText(queryPoint.gongcangName);
                                            break;
                                        case "学校":
                                            et_xxmz.setText(queryPoint.xuexiaoName);
                                            break;
                                        case "商铺":
                                            et_xxmz.setText(queryPoint.shangpuName);
                                            break;
                                    }
                                    Spinner et_mplx = dialog_view.findViewById(R.id.spinner_plateType);
                                    items = getResources().getStringArray(R.array.plateType);
                                    ArrayAdapter mAdapter1 = new TestArrayAdapter(MainActivity.this, items);
                                    et_mplx.setAdapter(mAdapter1);
                                    int pseletion = 0;
                                    for(String item : items){
                                        if(item.equals(queryPoint.plateType)){
                                            break;
                                        }
                                        pseletion++;
                                    }
                                    et_mplx.setSelection(pseletion);
                                    com.rey.material.widget.EditText et_comt = dialog_view.findViewById(R.id.eT_comments);
                                    et_comt.setText(queryPoint.comments);
                                    AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this)
                                            .setTitle("修改字段")
                                            .setView(dialog_view)
                                            .setPositiveButton("确定", (dialogInterface, i) -> {
                                                if (mselectedFeature == null)
                                                    return;
                                                //update db
                                                AsyncTask.execute(() -> {
                                                    Log.e("update Selected",et_pt.getSelectedItem().toString());
                                                    queryPoint.pointType = et_pt.getSelectedItem().toString();
                                                    queryPoint.jingwushi = et_jws.getText().toString();
                                                    queryPoint.cunjuwei = et_cjw.getText().toString();
                                                    queryPoint.jieluxiang = et_jlx.getText().toString();
                                                    queryPoint.menpaihao = et_mph.getText().toString();
                                                    queryPoint.jianzhuwumingchen = et_jzw.getText().toString();
                                                    queryPoint.loupaihao = et_lph.getText().toString();
                                                    queryPoint.loucengshu = et_lcs.getText().toString();
                                                    queryPoint.roomName = eT_rooms.getText().toString();
                                                    queryPoint.plateType = et_mplx.getSelectedItem().toString();
                                                    queryPoint.comments = et_comt.getText().toString();
                                                    queryPoint.danweiming = et_danweiming.getText().toString();
                                                    queryPoint.xiaoqudanyuan = et_xqdy.getText().toString();
                                                    switch (queryPoint.pointType){
                                                        case "小区":
                                                            queryPoint.xiaoquName = et_xxmz.getText().toString();
                                                            break;
                                                        case "医院":
                                                            queryPoint.yiyuanName = et_xxmz.getText().toString();
                                                            break;
                                                        case "工厂":
                                                            queryPoint.gongcangName = et_xxmz.getText().toString();
                                                            break;
                                                        case "学校":
                                                            queryPoint.xuexiaoName = et_xxmz.getText().toString();
                                                            break;
                                                        case "商铺":
                                                            queryPoint.shangpuName = et_xxmz.getText().toString();
                                                    }
                                                    mydb.gpointDao().updatePoint(queryPoint);

                                                });
                                                AsyncTask.execute(()->{
                                                    mapViewModel.updatePoint2SHP(result, queryPoint.jieluxiang, queryPoint.menpaihao);
                                                    mapViewModel.resetLabel();
                                                });
                                                dialogInterface.dismiss();
                                                mCallout.dismiss();
                                            }).setNegativeButton("取消", (dialogInterface, i) -> {
                                                dialogInterface.dismiss();
                                            });
                                    dialog.show();
                                });
                                imageView_move.setOnClickListener(view -> {
                                    Toast.makeText(MainActivity.this, "请移动点", Toast.LENGTH_SHORT).show();
                                    mCallout.dismiss();
                                    movingPoint = true;
                                });
                                imageView_imgs.setOnClickListener(view -> {
                                    View imgDialogView = View.inflate(MainActivity.this,R.layout.img_dialog,null);
                                    LinearLayout linearLayout = imgDialogView.findViewById(R.id.img_linearLayout);
                                    AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this)
                                            .setTitle("查看图片")
                                            .setView(imgDialogView)
                                            .setNeutralButton("加一", (dialogInterface, i) -> {
                                                //take photo and * update info to db! *
                                                tempGPoint = queryPoint;
                                                dispatchTakePictureIntent(queryPoint);
                                            });

                                    String[] picnames = queryPoint.picName.split(";");
                                    for(String picname : picnames ){
                                        ImageView imageView1 = new ImageView(MainActivity.this);
                                        try {
                                            FileInputStream fis = new FileInputStream(workPath+"/"+picname);///把流转化为Bitmap图片
                                            Bitmap bm = BitmapFactory.decodeStream(fis);
                                            imageView1.setImageBitmap(bm);
                                            imageView1.setOnClickListener(view1 -> {
                                                AlertDialog.Builder dialog1 = new AlertDialog.Builder(MainActivity.this)
                                                        .setTitle("你要删除图片吗？")
                                                        .setPositiveButton("删除！", (dialogInterface, i) -> {
                                                            String picPath = workPath+"/"+picname;
                                                            File file = new File(picPath);
                                                            if(file.delete()){
                                                                String newPicName="";
                                                                for(String pname : picnames){
                                                                    if(!pname.equals(picname))
                                                                        newPicName+=pname+";";
                                                                }
                                                                if(newPicName.length() > 0)
                                                                    newPicName = newPicName.substring(0,newPicName.length()-1);
                                                                queryPoint.picName = newPicName;
                                                                Toast.makeText(MainActivity.this,"删除成功！",Toast.LENGTH_SHORT).show();
                                                            }
                                                            dialogInterface.dismiss();
                                                        })
                                                        .setNegativeButton("取消", (dialogInterface, i) -> {
                                                            dialogInterface.dismiss();
                                                        });
                                                dialog1.show();
                                            });
                                        } catch (FileNotFoundException ex) {
                                            ex.printStackTrace();
                                        }
                                        linearLayout.addView(imageView1);
                                    }
                                    dialog.show();
                                });
                                imageView_delete.setOnClickListener(view -> {
                                    AsyncTask.execute(() -> {
                                        mydb.gpointDao().delete(queryPoint);

                                    });
                                    deletePointsFromShp(clickPoint);
                                    mCallout.dismiss();
                                });
                                // show CallOut
                                calloutLayout.addView(calloutContent);
                                calloutLayout.addView(imageView);
                                calloutLayout.addView(imageView_move);
                                calloutLayout.addView(imageView_imgs);
                                calloutLayout.addView(imageView_delete);
                                mCallout.setLocation(clickPoint);
                                mCallout.setContent(calloutLayout);
                                mCallout.show();
                            }
                        } catch (Exception e1) {
                            Log.e(getResources().getString(R.string.app_name), "Select feature failed: " + e1.getMessage());
                            e1.printStackTrace();
                        }
                    });
                }
                return super.onSingleTapConfirmed(e);
            }

            //长按添加点
            @Override
            public void onLongPress(MotionEvent v) {
                if (shapefileFeatureTable.isEditable() && shapefileFeatureTable.canAdd()) {
                    picCount = 0;
                    mapViewModel.getpicCount().setValue(0);
                    if (!overlay.getGraphics().isEmpty())
                        overlay.getGraphics().clear();
                    android.graphics.Point screenPoint = new android.graphics.Point(Math.round(v.getX()), Math.round(v.getY()));
                    Point mapPoint = mMapView.screenToLocation(screenPoint);

                    //add point info to db
                    SimpleMarkerSymbol markerSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.DIAMOND, Color.YELLOW, 10);
                    overlay.getGraphics().add(new Graphic(mapPoint, markerSymbol));

                    tempGPoint = point2Gpoint(mapPoint);//get the lon_lat and time info, as temp
                    //reset　some textView
                    com.rey.material.widget.EditText etfloor = findViewById(R.id.eT_floors);
                    etfloor.setText("");
                    com.rey.material.widget.EditText etcomments = findViewById(R.id.eT_comments);
                    etcomments.setText("");
                    com.rey.material.widget.EditText etshangpuming = findViewById(R.id.eT_shangpuName);
                    etshangpuming.setText("");
                    com.rey.material.widget.EditText etmph = findViewById(R.id.eT_menpaihao);
                    etmph.setText("");
                    com.rey.material.widget.EditText fjm = findViewById(R.id.eT_roomName);
                    fjm.setText("");
                    com.rey.material.widget.EditText xx = findViewById(R.id.eT_xxmingzi);
                    xx.setText("");
                    com.rey.material.widget.EditText lph = findViewById(R.id.eT_loupaihao);
                    lph.setText("");
                    com.rey.material.widget.EditText dy = findViewById(R.id.eT_xiaoqudanyuan);
                    dy.setText("");
                    com.rey.material.widget.EditText dw = findViewById(R.id.eT_danweiming);
                    dw.setText("");
                    com.rey.material.widget.EditText jws = findViewById(R.id.eT_jingwushi);
                    jws.setText("");
                    com.rey.material.widget.EditText cjw = findViewById(R.id.eT_cunjuwei);
                    cjw.setText("");
                    com.rey.material.widget.EditText jzw = findViewById(R.id.eT_jianzuwu);
                    jzw.setText("");

                    //默认蓝牌
                    spn_plateType.setSelection(0);
                    //open drawer to fill forms
                    drawerLayout.openDrawer(main_right_drawer_layout);
                } else {
                    Toast.makeText(MainActivity.this, "shp不可编辑", Toast.LENGTH_LONG).show();
                    Log.i("ShapefileEdit:", "The Shapefile cann't be edited");
                }
            }
        });
    }

    public void deletePointsFromShp(Point point){
        ShapefileFeatureTable shapefileFeatureTable = mapViewModel.getShapefileFeatureTable().getValue();
        ArcGISMap map = mMapView.getMap();
        int tolerance = 10;
        double mapTolerance = tolerance * mMapView.getUnitsPerDensityIndependentPixel();
        // use tolerance to create an envelope to query
        Envelope envelope = new Envelope(point.getX() - mapTolerance, point.getY() - mapTolerance,
                point.getX() + mapTolerance, point.getY() + mapTolerance, map.getSpatialReference());
        QueryParameters query = new QueryParameters();
        query.setGeometry(envelope);
        // request all available attribute fields
        final ListenableFuture<FeatureQueryResult> future = shapefileFeatureTable
                .queryFeaturesAsync(query);
        // add done loading listener to fire when the selection returns
        future.addDoneListener(() -> {
            try {
                //call get on the future to get the result
                FeatureQueryResult result = future.get();
                // create an Iterator
                Iterator<Feature> iterator = result.iterator();
                while (iterator.hasNext()) {
                    Log.e("moving", "" + iterator.hasNext());
                    Feature feature = iterator.next();
                    shapefileFeatureTable.deleteFeatureAsync(feature).addDoneListener(() -> tv_features.setText("shp已采集点数：" + shapefileFeatureTable.getTotalFeatureCount()));
                }
            }catch (Exception e){
                Log.e("delete point form shp",e.toString());
            }
        });
    }

    public MyGISPoint point2Gpoint(Point point) {
        String[] Lat_Lng = point2LATLNG(point);
        MyGISPoint gpoint = new MyGISPoint();
        gpoint.point = point;
        gpoint.pointType = getTextbyRid(R.id.spinner_pointType);
        gpoint.LAT = String.valueOf(Lat_Lng[0]);
        gpoint.LNG = String.valueOf(Lat_Lng[1]);
        Log.e("create point","lxly:"+Lat_Lng[0]+"|"+Lat_Lng[1]);
//        gpoint.jingwushi = getTextbyRid(R.id.eT_jingwushi);
//        gpoint.cunjuwei = getTextbyRid(R.id.eT_cunjuwei);
        gpoint.jieluxiang = getTextbyRid(R.id.eT_jieluxiang);
//        gpoint.menpaihao = getTextbyRid(R.id.eT_menpaihao);
//        gpoint.jianzhuwumingchen = getTextbyRid(R.id.eT_jianzuwu);
//        gpoint.loupaihao = getTextbyRid(R.id.eT_loupaihao);
//        gpoint.loucengshu = getTextbyRid(R.id.eT_floors);
        gpoint.plateType = getTextbyRid(R.id.spinner_plateType);
//        gpoint.comments = getTextbyRid(R.id.eT_comments);
        gpoint.workerID = getTextbyRid(R.id.eT_workerID);
//        gpoint.danweiming = getTextbyRid(R.id.eT_danweiming);
//        gpoint.shangpuName = getTextbyRid(R.id.eT_shangpuName);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");// HH:mm:ss
        SimpleDateFormat myDateFormat = new SimpleDateFormat("yyyyMMdd");// HH:mm:ss
        //SimpleDateFormat exif_simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");// HH:mm:ss
        //获取当前时间
        Date date = new Date(System.currentTimeMillis());
        gpoint.time = myDateFormat.format(date);
        //gpoint.exif_time = exif_simpleDateFormat.format(date);
        gpoint.picName="";
        gpoint.roomName = "";

        //生成md5编号
        gpoint.point_id = gpoint.workerID+"-"+simpleDateFormat.format(date);
        return gpoint;
    }

    public String[] point2LATLNG(Point point){
        String mLatLongDDValue = CoordinateFormatter.toLatitudeLongitude(point, CoordinateFormatter.LatitudeLongitudeFormat.DECIMAL_DEGREES,6);
        String[] LAT_LNG = mLatLongDDValue.split(" ");
        LAT_LNG[0] = LAT_LNG[0].substring(0, LAT_LNG[0].length()-1);
        LAT_LNG[1] = LAT_LNG[1].substring(0, LAT_LNG[1].length()-1);
        return  LAT_LNG;
    }

    public String getTextbyRid(int id) {
        View v = findViewById(id);
        if (v instanceof TextView) {
            EditText et = (EditText) v;
            return et.getText().toString();
        } else if (v instanceof Spinner) {
            Spinner spn = (Spinner) v;
            return spn.getSelectedItem().toString();
        }else if(v instanceof com.rey.material.widget.EditText){
            com.rey.material.widget.EditText et = (com.rey.material.widget.EditText)v;
            return et.getText().toString();
        }
        return "error";
    }

    String mCurrentPhotoPath;
    String mCurrentPhotoName;
    static final int REQUEST_TAKE_PHOTO = 1;

    @TargetApi(Build.VERSION_CODES.FROYO)
    private File createImageFile(String imgName) throws IOException {
        // Create an image file name
        File storageDir = new File(workPath);
        //File storageDir =getExternalFilesDir();
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }

        File image = File.createTempFile(
                imgName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
            // Save a file: path for use with ACTION_VIEW intents
            mCurrentPhotoPath = image.getAbsolutePath();
//        File image = new File(workPath+"/"+imgName);
//        if(image.createNewFile()){
//            return image;
//        }else
//            return null;

        return image;
    }

    private void dispatchTakePictureIntent(MyGISPoint gpoint) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            String imgName = tempGPoint.point_id+"_";
            mapViewModel.getpicCount().setValue(picCount);
            try {
                photoFile = createImageFile(imgName);
                mCurrentPhotoName = photoFile.getName();
                Log.e("currentPath",mCurrentPhotoPath);
            } catch (IOException ex) {
                // Error occurred while creating the File
                Toast.makeText(this, ex.toString(), Toast.LENGTH_SHORT).show();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                Log.e("currentPath",photoFile.getAbsolutePath());
            }
        }
    }

    @Override
    protected void onActivityResult(int  requestId, int resultCode, Intent data) {
        if (requestId == REQUEST_TAKE_PHOTO) {
            if (resultCode == RESULT_CANCELED) {
                Log.e("Camera", "Cancle");
                //delete temp file
                try {
                    File photo = new File(mCurrentPhotoPath);
                    if (photo.exists()) {
                        Log.e("Camera", "delete!");
                        if (photo.delete())
                            Log.e("Camera", "deleted sucess!");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (resultCode == RESULT_OK) {
                Log.e("Camera", "OK");
                picCount++;
                mapViewModel.getpicCount().setValue(picCount);
                //makeEXIF(mCurrentPhotoPath);
                if (tempGPoint.picName == "")
                    tempGPoint.picName += mCurrentPhotoName;
                else
                    tempGPoint.picName += ";" + mCurrentPhotoName;

                //only a point~ anyway
                AsyncTask.execute(() -> {
                    mydb.gpointDao().updatePoint(tempGPoint);
                });
            }
        }
        super.onActivityResult(requestId, resultCode, data);
    }

    public class TestArrayAdapter extends ArrayAdapter<String> {
        private Context mContext;
        private String [] mStringArray;
        public TestArrayAdapter(Context context, String[] stringArray) {
            super(context, android.R.layout.simple_spinner_item, stringArray);
            mContext = context;
            mStringArray=stringArray;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            //修改Spinner展开后的字体颜色
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(mContext);
                convertView = inflater.inflate(android.R.layout.simple_spinner_dropdown_item, parent,false);
            }

            //此处text1是Spinner默认的用来显示文字的TextView
            TextView tv = (TextView) convertView.findViewById(android.R.id.text1);
            tv.setText(mStringArray[position]);
            tv.setTextSize(22f);

            return convertView;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // 修改Spinner选择后结果的字体颜色
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(mContext);
                convertView = inflater.inflate(android.R.layout.simple_spinner_item, parent, false);
            }

            //此处text1是Spinner默认的用来显示文字的TextView
            TextView tv = (TextView) convertView.findViewById(android.R.id.text1);
            tv.setText(mStringArray[position]);
            tv.setTextSize(24);
            return convertView;
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.resume();
    }

    @Override
    protected void onDestroy() {

        mMapView.dispose();
        super.onDestroy();
    }

    private long exitTime = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){
            if((System.currentTimeMillis()-exitTime) > 2000){
                Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
