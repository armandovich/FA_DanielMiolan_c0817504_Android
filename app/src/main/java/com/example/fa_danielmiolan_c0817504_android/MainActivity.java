package com.example.fa_danielmiolan_c0817504_android;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button openMapBtn;
    private RecyclerView locationRV;
    private PlaceRVAdapter placeAdapter = new PlaceRVAdapter();
    private static final int REQUEST_CODE = 1;

    public static PlaceViewModel placeVM;
    public static List<Place> placesList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        placeVM = new ViewModelProvider(this).get(PlaceViewModel.class);

        openMapBtn =  findViewById(R.id.openMapBtn);
        locationRV = findViewById(R.id.locationRV);
        locationRV.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        locationRV.setHasFixedSize(true);

        locationRV.setAdapter(placeAdapter);

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                placeVM.delete(placesList.get(viewHolder.getAdapterPosition()));
                Toast.makeText(getApplicationContext(), "Place deleted.", Toast.LENGTH_LONG).show();
            }
        };

        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(locationRV);

        openMapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(myIntent);
            }
        });

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, REQUEST_CODE);

        loadPlaces();
    }

    private void loadPlaces() {
        placeVM.getAllPlaces().observe(this, new Observer<List<Place>>() {
            @Override
            public void onChanged(List<Place> places) {
                placesList.clear();
                placesList = places;
                placeAdapter.submitList(placesList);
                placeAdapter.notifyDataSetChanged();
            }
        });
    }
}