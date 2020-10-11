package com.gis.dy.dygismap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.ViewGroup;

import com.esri.arcgisruntime.data.ShapefileFeatureTable;

import java.util.ArrayList;

public class Query_SHP_Activity extends AppCompatActivity {
    private ShapefileFeatureTable shapefileFeatureTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query__shp_);

        Bundle bundle = getIntent().getExtras();
        ArrayList<String> list = bundle.getStringArrayList("query result");

        RecyclerView recyclerView = findViewById(R.id.rv_featuresList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setAdapter(new RecyclerView.Adapter() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return null;
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

            }

            @Override
            public int getItemCount() {
                return 0;
            }
        });
    }
}
