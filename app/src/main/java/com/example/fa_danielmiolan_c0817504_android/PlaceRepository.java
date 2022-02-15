package com.example.fa_danielmiolan_c0817504_android;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;

public class PlaceRepository {
    private PlaceDao placeDao;
    private LiveData<List<Place>> allPlaces;

    public PlaceRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        placeDao = db.placeDao();
        allPlaces = placeDao.getAll();
    }

    public void insert(Place place) {
        new AsyncInsert(placeDao).execute(place);
    }

    public void update(Place place) {
        new AsyncUpdate(placeDao).execute(place);
    }

    public void delete(Place place) {
        new AsyncDelete(placeDao).execute(place);
    }

    public LiveData<List<Place>> getAllPlaces() {
        return allPlaces;
    }

    private static class AsyncInsert extends AsyncTask<Place, Void, Void> {
        private PlaceDao placeAsyncDao;

        private AsyncInsert(PlaceDao dao) {
            this.placeAsyncDao = dao;
        }

        @Override
        protected Void doInBackground(Place... place) {
            placeAsyncDao.insert(place[0]);
            return null;
        }
    }

    private static class AsyncUpdate extends AsyncTask<Place, Void, Void> {
        private PlaceDao placeAsyncDao;

        private AsyncUpdate(PlaceDao dao) {
            this.placeAsyncDao = dao;
        }

        @Override
        protected Void doInBackground(Place... place) {
            placeAsyncDao.update(place[0]);
            return null;
        }
    }

    private static class AsyncDelete extends AsyncTask<Place, Void, Void> {
        private PlaceDao placeAsyncDao;

        private AsyncDelete(PlaceDao dao) {
            this.placeAsyncDao = dao;
        }

        @Override
        protected Void doInBackground(Place... place) {
            placeAsyncDao.delete(place[0]);
            return null;
        }
    }
}
