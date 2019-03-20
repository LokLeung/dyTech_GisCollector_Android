package com.gis.dy.dygismap.repository.room;
import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import com.esri.arcgisruntime.geometry.Point;

@Entity(tableName = "tb_points")
public class PointEntity {
    @Ignore
    public Point point;
    @Ignore
    public String exif_time;

    @PrimaryKey(autoGenerate = true)
    private long id;        //0
    @ColumnInfo
    public String pointType;
    @ColumnInfo
    public String point_id;
    @ColumnInfo
    public String LAT;
    @ColumnInfo
    public String LNG;
    @ColumnInfo
    public String jingwushi;
    @ColumnInfo
    public String cunjuwei;
    @ColumnInfo
    public String jieluxiang;
    @ColumnInfo
    public String menpaihao;
    @ColumnInfo
    public String xiaoquName;
    @ColumnInfo
    public String gongcangName;
    @ColumnInfo
    public String xuexiaoName;
    @ColumnInfo
    public String yiyuanName;
    @ColumnInfo
    public  String shangpuName;

    public String getShangpuName() {
        return shangpuName;
    }

    public void setShangpuName(String shangpuName) {
        this.shangpuName = shangpuName;
    }

    @ColumnInfo
    public String jianzhuwumingchen;
    @ColumnInfo
    public String danweiming;

    public String getDanweiming() {
        return danweiming;
    }

    public void setDanweiming(String danweiming) {
        this.danweiming = danweiming;
    }

    @ColumnInfo
    public String loucengshu;
    @ColumnInfo
    public String xiaoqudanyuan;
    @ColumnInfo
    public String roomName;
    @ColumnInfo
    public String plateType;
    @ColumnInfo
    public String comments;
    @ColumnInfo
    public String time;
    @ColumnInfo
    public String picName;
    @ColumnInfo
    public String workerID;


    public Point getPoint() {
        return point;
    }
    public long getId() { return id; }

    public void setId(long id) {
        this.id = id;
    }

    public void setPoint(Point point) {
        this.point = point;
    }

    public String getPointType() {
        return pointType;
    }

    public void setPointType(String pointType) {
        this.pointType = pointType;
    }

    public String getPoint_id() {
        return point_id;
    }

    public void setPoint_id(String point_id) {
        this.point_id = point_id;
    }

    public String getLAT() {
        return LAT;
    }

    public void setLAT(String LAT) {
        this.LAT = LAT;
    }

    public String getLNG() {
        return LNG;
    }

    public void setLNG(String LNG) {
        this.LNG = LNG;
    }

    public String getJingwushi() {
        return jingwushi;
    }

    public void setJingwushi(String jingwushi) {
        this.jingwushi = jingwushi;
    }

    public String getCunjuwei() {
        return cunjuwei;
    }

    public void setCunjuwei(String cunjuwei) {
        this.cunjuwei = cunjuwei;
    }

    public String getJieluxiang() {
        return jieluxiang;
    }

    public void setJieluxiang(String jieluxiang) {
        this.jieluxiang = jieluxiang;
    }

    public String getMenpaihao() {
        return menpaihao;
    }

    public void setMenpaihao(String menpaihao) {
        this.menpaihao = menpaihao;
    }

    public String getJianzhuwumingchen() {
        return jianzhuwumingchen;
    }

    public void setJianzhuwumingchen(String jianzhuwumingchen) {
        this.jianzhuwumingchen = jianzhuwumingchen;
    }

    public String getLoucengshu() {
        return loucengshu;
    }

    public void setLoucengshu(String loucengshu) {
        this.loucengshu = loucengshu;
    }

    public String getPlateType() {
        return plateType;
    }

    public void setPlateType(String plateType) {
        this.plateType = plateType;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getPicName() {
        return picName;
    }

    public void setPicName(String picName) {
        this.picName = picName;
    }

    public String getWorkerID() {
        return workerID;
    }

    public void setWorkerID(String workerID) {
        this.workerID = workerID;
    }

    public String getXiaoquName() {
        return xiaoquName;
    }

    public void setXiaoquName(String xiaoquName) {
        this.xiaoquName = xiaoquName;
    }

    public String getGongcangName() {
        return gongcangName;
    }

    public void setGongcangName(String gongcangName) {
        this.gongcangName = gongcangName;
    }

    public String getXuexiaoName() {
        return xuexiaoName;
    }

    public void setXuexiaoName(String xuexiaoName) {
        this.xuexiaoName = xuexiaoName;
    }

    public String getYiyuanName() {
        return yiyuanName;
    }

    public void setYiyuanName(String yiyuanName) {
        this.yiyuanName = yiyuanName;
    }

    public String getXiaoqudanyuan() {
        return xiaoqudanyuan;
    }

    public void setXiaoqudanyuan(String xiaoqudanyuan) {
        this.xiaoqudanyuan = xiaoqudanyuan;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

}

