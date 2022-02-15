package com.example.fa_danielmiolan_c0817504_android;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.fa_danielmiolan_c0817504_android.databinding.ActivityMapsBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerDragListener {
    private Geocoder geocoder;
    private LatLng userLocation = new LatLng(0,0);
    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private FusedLocationProviderClient locationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private boolean didZoomToUser = false;
    private boolean editModeActive = false;
    private Place selectedPlace = new Place();
    private Marker currentMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        binding.mapType.setSelection(1);

        binding.mapType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                mMap.setMapType(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });

        locationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        geocoder = new Geocoder(this, Locale.getDefault());

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MapsActivity.super.onBackPressed();
            }
        });

        binding.savePlaceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                savePlace(view);
            }
        });

        binding.deletePlaceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deletePlace(view);
            }
        });


        binding.searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMap.clear();
                Intent i = getIntent();
                String type = binding.searchPlaceInput.getText().toString();
                RequestQueue queue = Volley.newRequestQueue(view.getContext());


                StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
                googlePlacesUrl.append("location=").append(userLocation.latitude).append(",").append(userLocation.longitude);
                googlePlacesUrl.append("&radius=").append(100);
                googlePlacesUrl.append("&types=").append(type);
                googlePlacesUrl.append("&sensor=true");
                googlePlacesUrl.append("&key=" + getResources().getString(R.string.map_key));

                Log.i("DEBUG", "" + googlePlacesUrl.toString());

                JsonObjectRequest request = new JsonObjectRequest(googlePlacesUrl.toString(),
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject result) {
                                Log.i("DEBUG", "onResponse: Result= " + result.toString());
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e("DEBUG", "onErrorResponse: Error= " + error);
                                Log.e("DEBUG", "onErrorResponse: Error= " + error.getMessage());
                                // GETTING NEXT ERROR
                                // "You must enable Billing on the Google Cloud Project at"
                            }
                        }
                );

                queue.add(request);
            }
        });

        Place tempPlace = (Place) getIntent().getSerializableExtra("Place");

        if (tempPlace != null) {
            editModeActive = true;
            selectedPlace = tempPlace;
            binding.placeAddressInput.setText(tempPlace.getAddress());
            binding.placeLatInput.setText("" + tempPlace.getLatitude());
            binding.placeLongInput.setText("" + tempPlace.getLongitude());
            binding.placeVisitSwitch.setChecked(tempPlace.getStatus());
        } else {
            binding.deletePlaceBtn.setEnabled(false);
            binding.deletePlaceBtn.setAlpha(0);
        }
    }

    private void savePlace(View view) {
        String address = binding.placeAddressInput.getText().toString();
        String latStr = binding.placeLatInput.getText().toString();
        String lonStr = binding.placeLongInput.getText().toString();
        boolean status = binding.placeVisitSwitch.isChecked();
        double lat = latStr.equals("") ? 0 : Double.parseDouble(latStr);
        double lon = lonStr.equals("") ? 0 : Double.parseDouble(lonStr);

        if(address.equals("Undefined")) {
            Toast.makeText(view.getContext(), "Place address can't be empty.", Toast.LENGTH_LONG).show();
            return;
        }

        Place place = new Place();
        place.setAddress(address);
        place.setStatus(status);
        place.setLatitude(lat);
        place.setLongitude(lon);

        if (editModeActive) {
            place.setId(selectedPlace.getId());
            MainActivity.placeVM.update(place);
            Toast.makeText(view.getContext(), "Place updated.", Toast.LENGTH_LONG).show();
        } else {
            MainActivity.placeVM.insert(place);
            Toast.makeText(view.getContext(), "Place saved.", Toast.LENGTH_LONG).show();
            clearInputs();
        }
    }

    private void deletePlace(View view) {
        editModeActive = false;
        MainActivity.placeVM.delete(selectedPlace);
        Toast.makeText(view.getContext(), "Place deleted.", Toast.LENGTH_LONG).show();
        clearInputs();
    }

    private void clearInputs() {
        binding.placeAddressInput.setText("Undefined");
        binding.placeLatInput.setText("0.0");
        binding.placeLongInput.setText("0.0");
        binding.placeVisitSwitch.setChecked(false);

        mMap.clear();
        drawUserMarker();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 10));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMarkerDragListener(this);

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                generateMarkerFromClick(latLng);
            }
        });

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                generateMarkerFromClick(latLng);
            }
        });

        if (hasLocationPermission()) {
            updateUserLocation();
        }
    }

    private void generateMarkerFromClick(LatLng latLng) {
        String addressStr = getAddressFromLocation(latLng);

        binding.placeAddressInput.setText(addressStr);
        binding.placeLatInput.setText("" + latLng.latitude);
        binding.placeLongInput.setText("" + latLng.longitude);

        mMap.clear();
        drawUserMarker();
        drawPlaceMarker(latLng, addressStr);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
    }

    private boolean hasLocationPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void updateUserLocation() {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    Location location = locationResult.getLastLocation();
                    userLocation = new LatLng(location.getLatitude(), location.getLongitude());

                    if (!didZoomToUser) {
                        if (selectedPlace.getId() > 0) {
                            LatLng latLng = new LatLng(selectedPlace.getLatitude(), selectedPlace.getLongitude());
                            drawPlaceMarker(latLng, selectedPlace.getAddress());
                        }

                        didZoomToUser = true;
                        drawUserMarker();
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 10));
                    }
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private void drawPlaceMarker(LatLng latLng, String address) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng).title(address).draggable(true);

        currentMarker = mMap.addMarker(markerOptions);
    }

    private void drawUserMarker() {
        mMap.addMarker(new MarkerOptions().position(userLocation).title("your location!").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
    }

    private String getAddressFromLocation(LatLng latLng) {
        try {
            return  "" + geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1).get(0).getAddressLine(0);
        } catch (IOException e) {
            e.printStackTrace();
            return "Undefined";
        }
    }

    @Override
    public void onMarkerDragStart(Marker marker) { }

    @Override
    public void onMarkerDrag(Marker marker) { }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        LatLng pos = marker.getPosition();
        String address = marker.getTitle();

        if(currentMarker != null) {
            currentMarker.remove();
        }

        drawPlaceMarker(pos, address);
    }
}