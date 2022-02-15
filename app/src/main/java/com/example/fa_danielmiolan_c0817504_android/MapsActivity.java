package com.example.fa_danielmiolan_c0817504_android;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

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
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private Geocoder geocoder;
    private List<Address> addresses;
    private LatLng userLocation = new LatLng(0,0);
    private Button saveBtn, deleteBtn, backBtn;
    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private FusedLocationProviderClient locationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private boolean didZoomToUser = false;
    private boolean editModeActive = false;
    private Place selectedPlace = new Place();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


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

    private void clearInputs() {
        binding.placeAddressInput.setText("Undefined");
        binding.placeLatInput.setText("0.0");
        binding.placeLongInput.setText("0.0");
        binding.placeVisitSwitch.setChecked(false);

        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(userLocation).title("your location!").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 10));
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);

                try {
                    addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                    String addressStr = "" + addresses.get(0).getAddressLine(0);
                    markerOptions.title(addressStr);
                    binding.placeAddressInput.setText(addressStr);

                } catch (IOException e) {
                    markerOptions.title(latLng.latitude + " : " + latLng.longitude);
                    e.printStackTrace();
                }

                binding.placeLatInput.setText("" + latLng.latitude);
                binding.placeLongInput.setText("" + latLng.longitude);

                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(userLocation).title("your location!").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
                mMap.addMarker(markerOptions);
            }
        });

        if (hasLocationPermission()) {
            updateUserLocation();
        }
        // Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
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
                        didZoomToUser = true;
                        mMap.addMarker(new MarkerOptions().position(userLocation).title("your location!").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
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
}