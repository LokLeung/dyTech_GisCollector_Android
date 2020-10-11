package com.gis.dy.dygismap.repository.room;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

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
