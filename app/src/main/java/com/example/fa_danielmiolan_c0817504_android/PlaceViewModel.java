package com.example.fa_danielmiolan_c0817504_android;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class PlaceViewModel extends AndroidViewModel {
    private PlaceRepository placeRepo;
    private LiveData<List<Place>> allPlaces;

    public PlaceViewModel(@NonNull Application application) {
        super(application);
        placeRepo = new PlaceRepository(application);
        allPlaces = placeRepo.getAllPlaces();
    }

    public LiveData<List<Place>> getAllPlaces() {
        return allPlaces;
    }

    public void insert(Place place) {
        placeRepo.insert(place);
    }
    public void update(Place place) {
        placeRepo.update(place);
    }
    public void delete(Place place) { placeRepo.delete(place); }
}
