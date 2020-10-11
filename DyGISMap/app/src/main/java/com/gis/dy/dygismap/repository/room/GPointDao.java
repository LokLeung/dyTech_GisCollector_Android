package com.gis.dy.dygismap.repository.room;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import android.database.Cursor;

import com.gis.dy.dygismap.model.MyGISPoint;

import java.util.List;

@Dao
public interface GPointDao {

    @Query("SELECT * FROM tb_points")
    Cursor getAll();

    @Query("SELECT * FROM tb_points")
    List<MyGISPoint> getAll2List();

//    @Query("SELECT * FROM tb_points WHERE roomName is null OR roomName = ''")
//    List<MyGISPoint> getAllMain2List();

//    @Query("SELECT * FROM tb_points WHERE roomName is not null And roomName != '' ")
//    List<MyGISPoint> getAllRoom2List();

//    @Query("SELECT count(*) FROM tb_points")
//    int getCount();

    @Query("SELECT count(id) FROM tb_points")
    LiveData<Integer> getCountLD();

//    @Query("SELECT * FROM tb_points WHERE time = :query_time")
//    MyGISPoint findByTime(String query_time);

//    @Query("SELECT * FROM tb_points WHERE time = :query_time and roomName is null OR roomName = '' ")
//    MyGISPoint findMainPointByTime(String query_time);

    @Query("SELECT * FROM tb_points WHERE time = :query_time and roomName is not null And roomName != '' ")
    List<MyGISPoint> findRoomPointByTime(String query_time);

    @Query("SELECT * FROM tb_points WHERE LAT = :lat and LNG = :lng")
    MyGISPoint findByLatLnt(String lat, String lng);

    @Query("SELECT count(id) FROM tb_points WHERE id = :ID")
    int findById(long ID);

    @Query("DELETE FROM tb_points")
    void clearTable();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertPoint(MyGISPoint... gpoints);

    @Update
    void updatePoint(MyGISPoint... gpoints);

    @Delete
    void delete(MyGISPoint... gpoints);
}
