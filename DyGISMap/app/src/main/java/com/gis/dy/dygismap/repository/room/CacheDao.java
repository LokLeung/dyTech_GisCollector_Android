package com.gis.dy.dygismap.repository.room;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface CacheDao {

    @Query("SELECT count(*) FROM tb_cache")
    int getCount();

    @Query("SELECT * FROM tb_cache")
    List<CacheEntity> getLastCache();

    @Insert
    void insertCache(CacheEntity... cache);

    @Update
    void updateCache(CacheEntity... cache);

    @Delete
    void delete(CacheEntity... cache);
}
