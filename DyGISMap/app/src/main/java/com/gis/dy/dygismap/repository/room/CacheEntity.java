package com.gis.dy.dygismap.repository.room;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "tb_cache")
public class CacheEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;        //
    @ColumnInfo
    private String last_tilemap;    //底图
    @ColumnInfo
    private String last_worker;    //内部编号

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLast_tilemap() {
        return last_tilemap;
    }

    public void setLast_tilemap(String last_tilemap) {
        this.last_tilemap = last_tilemap;
    }

    public String getLast_worker() {
        return last_worker;
    }

    public void setLast_worker(String last_worker) {
        this.last_worker = last_worker;
    }
}
