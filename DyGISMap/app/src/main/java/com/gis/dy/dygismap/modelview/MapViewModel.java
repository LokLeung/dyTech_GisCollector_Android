package com.gis.dy.dygismap.modelview;

import android.Manifest;
import android.app.Activity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.esri.arcgisruntime.arcgisservices.LabelDefinition;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureQueryResult;
import com.esri.arcgisruntime.data.ShapefileFeatureTable;
import com.esri.arcgisruntime.data.TileCache;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.layers.ArcGISTiledLayer;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.esri.arcgisruntime.symbology.SimpleRenderer;
import com.esri.arcgisruntime.symbology.TextSymbol;
import com.gis.dy.dygismap.MainActivity;
import com.gis.dy.dygismap.model.MyGISPoint;
import com.gis.dy.dygismap.model.mapmodel.MyMapModel;
import com.gis.dy.dygismap.repository.room.MyRoomDataBase;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MapViewModel extends ViewModel {
    //Model
    MyMapModel mapModel;
    public final int TILEMAP = 0;
    public final int SHPMAP = 1;
    public String tileCachePath;

    //context things
    private final static String TAG = MainActivity.class.getSimpleName();
    private Activity activity;

    //arcgis things
    private MutableLiveData<ShapefileFeatureTable> shapefileFeatureTable;
    private MutableLiveData<ArcGISMap> map;
    private MutableLiveData<FeatureLayer> featureLayer;
    private MutableLiveData<Long> featureTotalCount;
    private MutableLiveData<Integer> picCount;

    //db 临时放这测试
    private MutableLiveData<Integer> dbCount;

    public MapViewModel(Activity activity){
        this.activity = activity;
        mapModel = new MyMapModel();
    }

    /**
     * Request read permission on the device.
     */
    public void requestReadPermission(String filePath, int type) {
        // define permission to request
        String[] reqPermission = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
        int requestCode = 2;
        // For API level 23+ request permission at runtime
        if (ContextCompat.checkSelfPermission(activity,
                reqPermission[0]) == PackageManager.PERMISSION_GRANTED) {
            if (type == 1)
                featureLayerShapefile(filePath);
            else if (type == 0)
                tiledLayerTpkfile(filePath);
        } else {
            // request permission
            ActivityCompat.requestPermissions(activity, reqPermission, requestCode);
        }
    }

    private void featureLayerShapefile(String filePath) {
        // load the shapefile with a local path
        ShapefileFeatureTable featureTable = new ShapefileFeatureTable(filePath);
        featureTable.loadAsync();
        featureTable.addDoneLoadingListener(() -> {
            if (featureTable.getLoadStatus() == LoadStatus.LOADED) {
                // create a feature layer to display the shapefile
                FeatureLayer featureLayer = new FeatureLayer(featureTable);
                setFeatureLayerLabel(featureLayer);

                //update the layer data
                this.shapefileFeatureTable.setValue(featureTable);
                this.featureLayer.setValue(featureLayer);

                //set model
                mapModel.setShapefileFeatureTable(featureTable);
                mapModel.setFeatureLayer(featureLayer);

                //init data from db
                try{
                    initSHP();
                }catch (Exception e){
                    Log.e("MapViewModel","init shp data failed:"+e.toString());
                }
            } else {
                String error = "shp文件读取失败: " + featureTable.getLoadError().toString();
                Toast.makeText(activity, error, Toast.LENGTH_LONG).show();
                Log.e(TAG, error);
            }
        });
    }

    private void tiledLayerTpkfile(String filePath) {
        tileCachePath = filePath;
        TileCache tileCache = new TileCache(filePath);
        ArcGISTiledLayer tiledLayer = new ArcGISTiledLayer(tileCache);
        Basemap basemap = new Basemap(tiledLayer);
        ArcGISMap map = new ArcGISMap(basemap);
        this.map.setValue(map);
    }

    public void setFeatureLayerLabel(FeatureLayer featureLayer) {
        // use large blue text with a yellow halo for the labels
        TextSymbol textSymbol = new TextSymbol();
        textSymbol.setSize(15);
        textSymbol.setColor(0xFF0000FF);
        textSymbol.setHaloColor(0xFFFFFF00);
        textSymbol.setHaloWidth(2);

        // construct the label definition json
        JsonObject json = new JsonObject();
        // prepend 'I - ' (for Interstate) to the route number for the label
        JsonObject expressionInfo = new JsonObject();
        expressionInfo.add("expression", new JsonPrimitive("$feature.menpaihao"));
        json.add("labelExpressionInfo", expressionInfo);
        // position the label above and along the direction of the road
        json.add("labelPlacement", new JsonPrimitive("esriServerLinePlacementAboveAlong"));
        // only show labels on the interstate highways (others have an empty rte_num1 attribute)
        //json.add("where", new JsonPrimitive("PointType <> ' '"));
        // set the text symbol as the label symbol
        json.add("symbol", new JsonParser().parse(textSymbol.toJson()));

        // create a label definition from the JSON string
        LabelDefinition labelDefinition = LabelDefinition.fromJson(json.toString());
        // add the definition to the feature layer and enable labels on it
        featureLayer.getLabelDefinitions().add(labelDefinition);
        featureLayer.setLabelsEnabled(true);

        // create a new simple renderer for the line feature layer
        SimpleMarkerSymbol lineSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.TRIANGLE, Color.rgb(255, 0, 0), 10);
        SimpleRenderer simpleRenderer = new SimpleRenderer(lineSymbol);

//        BitmapDrawable pinStarBlueDrawable = (BitmapDrawable) ContextCompat.getDrawable(activity, R.drawable.ic_location_on_black_24dp);
//        final PictureMarkerSymbol pinStarBlueSymbol = new PictureMarkerSymbol(pinStarBlueDrawable);
//        pinStarBlueSymbol.setHeight(40);
//        pinStarBlueSymbol.setWidth(40);
//        SimpleRenderer a = new SimpleRenderer(pinStarBlueSymbol);
        // override the current renderer with the new renderer defined above
        featureLayer.setRenderer(simpleRenderer);
    }

    public void addPoint2SHP(MyGISPoint gisPoint) {
        ShapefileFeatureTable shapefileFeatureTable = this.shapefileFeatureTable.getValue();
        if (shapefileFeatureTable.isEditable() && shapefileFeatureTable.canAdd()) {
            Point testPoint = new Point(Double.valueOf(gisPoint.LNG),Double.valueOf(gisPoint.LAT), SpatialReference.create(4326));

            // 构建待增加的Feature对象，设置几何，设置属性
            Feature feature = shapefileFeatureTable.createFeature();
            feature.setGeometry(testPoint);

            //添加数据到table()
            feature.getAttributes().put("time", gisPoint.time);//放入字段pointType
//            feature.getAttributes().put("PointType", gisPoint.pointType);//放入字段pointType
            feature.getAttributes().put("point_id", gisPoint.point_id);//放入字段内部id
//            feature.getAttributes().put("jingwushi", gisPoint.jingwushi);//放入字段pointType
//            feature.getAttributes().put("cunjuwei", gisPoint.cunjuwei);//放入字段pointType
            //feature.getAttributes().put("jieluxiang", gisPoint.jieluxiang);//放入字段pointType
            feature.getAttributes().put("menpaihao", gisPoint.menpaihao);//放入字段pointType
//            feature.getAttributes().put("jianzhuwu", gisPoint.jianzhuwumingchen);//放入字段pointType
//            feature.getAttributes().put("loucengshu", gisPoint.loucengshu);//放入字段pointType
//            feature.getAttributes().put("plateType", gisPoint.plateType);//放入字段pointType
//            feature.getAttributes().put("comments", gisPoint.comments);//放入字段pointType
//            feature.getAttributes().put("pic_name", gisPoint.picName);//放入字段pointType
//            feature.getAttributes().put("xiaoqu", gisPoint.xiaoquName);//放入字段pointType
//            feature.getAttributes().put("gongcang", gisPoint.gongcangName);//放入字段pointType
//            feature.getAttributes().put("yiyuan", gisPoint.yiyuanName);//放入字段pointType
//            feature.getAttributes().put("xuexiao", gisPoint.xuexiaoName);//放入字段pointType
//            feature.getAttributes().put("danyuan", gisPoint.xiaoqudanyuan);//放入字段pointType
//            feature.getAttributes().put("fangjian", gisPoint.roomName);//放入字段pointType

            // 调用addFeatureAsync方法增加要素
            final ListenableFuture<Void> addFeatureOper = shapefileFeatureTable.addFeatureAsync(feature);
            // 在操作完成的监听事件中判断操作是否成功
//            addFeatureOper.addDoneListener(() -> {
//                try {
//                    addFeatureOper.get();
//                    if (addFeatureOper.isDone()) {
//                        //Log.i("ShapefileEdit:", "Feature added!");
//                        picCount.setValue(0);
//                    }
//                } catch (InterruptedException | ExecutionException interruptedExceptionException) {
//                    // 处理异常
//                    Toast.makeText(activity.getApplicationContext(), "添加要素失败!" + interruptedExceptionException.toString(), Toast.LENGTH_LONG).show();
//                }
//            });
        } else {
            Toast.makeText(activity.getApplicationContext(), "shp不可编辑", Toast.LENGTH_LONG).show();
            Log.i("ShapefileEdit:", "The Shapefile cann't be edited");
        }
    }

    public void resetLabel(){
        featureLayer.getValue().setLabelsEnabled(false);
        featureLayer.getValue().setLabelsEnabled(true);
    }

    public void updatePoint2SHP(FeatureQueryResult result, String jieluxiang, String menpaihao){
        ShapefileFeatureTable shapefileFeatureTable = this.shapefileFeatureTable.getValue();
        Iterator<Feature> iterator = result.iterator();
        while (iterator.hasNext()) {
            Feature feature = iterator.next();
            Map<String, Object> attr = feature.getAttributes();
            attr.put("jieluxiang",jieluxiang);
            attr.put("menpaihao",menpaihao);
            shapefileFeatureTable.updateFeatureAsync(feature);
        }
    }

    public void initSHP(){
        if(mapModel.getFeatureLayer()!=null){
            //init all points in ab to shp
            AsyncTask.execute(() -> {
                List<MyGISPoint> gpoints = MyRoomDataBase.getInstance(activity.getApplicationContext()).gpointDao().getAll2List();
                Log.e("add points to shp","count:"+gpoints.size());
                for(MyGISPoint gpoint : gpoints){
                    addPoint2SHP(gpoint);
                }
            });
        }
    }

    public MyMapModel getMapModel() {
        return mapModel;
    }

    public MutableLiveData<ShapefileFeatureTable> getShapefileFeatureTable() {
        if(shapefileFeatureTable == null){
            shapefileFeatureTable = new MutableLiveData<>();
        }
        return shapefileFeatureTable;
    }

    public MutableLiveData<Integer> getpicCount() {
        if(picCount == null){
            picCount = new MutableLiveData<>();
        }
        return picCount;
    }

    public MutableLiveData<FeatureLayer> getfeatureLayer() {
        if(featureLayer == null){
            featureLayer = new MutableLiveData<>();
        }
        return featureLayer;
    }

    public MutableLiveData<ArcGISMap> getMap() {
        if(map == null){
            map = new MutableLiveData<>();
        }
        return map;
    }

    public MutableLiveData<Integer> getDbCount() {
        if(dbCount == null){
            dbCount = new MutableLiveData<>();
        }
        return dbCount;
    }
}
