package com.gis.dy.dygismap;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gis.dy.dygismap.model.MyGISPoint;
import com.gis.dy.dygismap.repository.MyDataBase;
import com.gis.dy.dygismap.repository.room.MyRoomDataBase;

import java.util.List;

public class EditFormActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_form);

        LinearLayout linearLayout = findViewById(R.id.layout_table);

        MyDataBase dataBase = new MyDataBase(EditFormActivity.this);

    }
}
