package com.gis.dy.dygismap.model.mapmodel;

import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.ShapefileFeatureTable;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.mapping.view.Callout;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;

public class MyMapModel {
    private FeatureLayer featureLayer;
    private ShapefileFeatureTable shapefileFeatureTable;

    public FeatureLayer getFeatureLayer() {
        return featureLayer;
    }

    public void setFeatureLayer(FeatureLayer featureLayer) {
        this.featureLayer = featureLayer;
    }

    public ShapefileFeatureTable getShapefileFeatureTable() {
        return shapefileFeatureTable;
    }

    public void setShapefileFeatureTable(ShapefileFeatureTable shapefileFeatureTable) {
        this.shapefileFeatureTable = shapefileFeatureTable;
    }
}
