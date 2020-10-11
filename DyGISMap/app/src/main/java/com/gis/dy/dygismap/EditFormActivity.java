package com.gis.dy.dygismap;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.LinearLayout;

import com.gis.dy.dygismap.repository.MyDataBase;

public class EditFormActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_form);

        LinearLayout linearLayout = findViewById(R.id.layout_table);

        MyDataBase dataBase = new MyDataBase(EditFormActivity.this);

    }
}
