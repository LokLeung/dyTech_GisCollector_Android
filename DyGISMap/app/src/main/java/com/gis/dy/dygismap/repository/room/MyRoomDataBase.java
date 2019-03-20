package com.gis.dy.dygismap.repository.room;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;
import android.support.annotation.NonNull;

import com.gis.dy.dygismap.model.MyGISPoint;


@Database(entities = {MyGISPoint.class, CacheEntity.class}, version = 8,exportSchema = false)
public abstract class MyRoomDataBase extends RoomDatabase {

    public static final String DB_NAME = "TestDatabase.db";
    private static volatile MyRoomDataBase instance;

    public static synchronized MyRoomDataBase getInstance(Context context) {
    if (instance == null) {
        instance = create(context);

    }
    return instance;
}

    private static final Migration MIGRATION_5_7 = new Migration(5,7) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
        }
    };
    private static final Migration MIGRATION_6_7 = new Migration(6,7) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
        }
    };
    private static final Migration MIGRATION_4_7 = new Migration(4,7) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
        }
    };
    private static final Migration MIGRATION_7_7 = new Migration(7,7) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
        }
    };
    private static final Migration MIGRATION_5_8 = new Migration(5,8) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
        }
    };
    private static final Migration MIGRATION_6_8= new Migration(6,8) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
        }
    };
    private static final Migration MIGRATION_4_8 = new Migration(4,8) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
        }
    };
    private static final Migration MIGRATION_7_8 = new Migration(7,8) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
        }
    };
    private static final Migration MIGRATION_8_8 = new Migration(8,8) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
        }
    };

    private static MyRoomDataBase create(final Context context) {
        return Room.databaseBuilder(
                context,
                MyRoomDataBase.class,
                DB_NAME)
                .addMigrations(MIGRATION_4_7,MIGRATION_5_7,MIGRATION_6_7,MIGRATION_7_7,MIGRATION_5_8,MIGRATION_6_8,MIGRATION_7_8,MIGRATION_8_8,MIGRATION_4_8)
                .allowMainThreadQueries()
                .build();
    }


    public abstract GPointDao gpointDao();

    public abstract CacheDao cacheDao();
}
