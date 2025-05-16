package com.example.enhancelostandfoundapp;


import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CreateAdvertActivity extends AppCompatActivity {

    private static final int AUTOCOMPLETE_REQUEST_CODE = 1;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 2;

    private RadioGroup postTypeRadioGroup;
    private RadioButton lostRadioButton;
    private RadioButton foundRadioButton;
    private TextInputEditText nameEditText;
    private TextInputEditText phoneEditText;
    private TextInputEditText descriptionEditText;
    private TextInputEditText dateEditText;
    private TextInputEditText locationEditText;
    private Button getCurrentLocationButton;
    private Button saveButton;

    private DatabaseHelper databaseHelper;
    private Calendar selectedDate = Calendar.getInstance();
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());

    private FusedLocationProviderClient fusedLocationClient;
    private double currentLatitude = 0;
    private double currentLongitude = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_advert);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Create Advert");
        }

        // Initialize Places API
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyBUgZSZ29s40fdHednB7FEVXOk3jbqUC1g");
        }

        // Initialize location provider
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize database helper
        databaseHelper = new DatabaseHelper(this);

        postTypeRadioGroup = findViewById(R.id.postTypeRadioGroup);
        lostRadioButton = findViewById(R.id.lostRadioButton);
        foundRadioButton = findViewById(R.id.foundRadioButton);
        nameEditText = findViewById(R.id.nameEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        dateEditText = findViewById(R.id.dateEditText);
        locationEditText = findViewById(R.id.locationEditText);
        getCurrentLocationButton = findViewById(R.id.getCurrentLocationButton);
        saveButton = findViewById(R.id.saveButton);

        dateEditText.setText(dateFormatter.format(selectedDate.getTime()));

        dateEditText.setOnClickListener(v -> showDatePickerDialog());

        locationEditText.setOnClickListener(v -> startPlacesAutocomplete());

        getCurrentLocationButton.setOnClickListener(v -> requestCurrentLocation());

        saveButton.setOnClickListener(v -> saveItem());
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, monthOfYear, dayOfMonth) -> {
                    selectedDate.set(Calendar.YEAR, year);
                    selectedDate.set(Calendar.MONTH, monthOfYear);
                    selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    dateEditText.setText(dateFormatter.format(selectedDate.getTime()));
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void startPlacesAutocomplete() {
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);

        // Start the autocomplete intent
        Intent intent = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.OVERLAY, fields)
                .build(this);
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
    }

    private void requestCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Permission already granted, get location
            getCurrentLocation();
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Getting emulator location...", Toast.LENGTH_SHORT).show();

        // Create a location request specifically configured for emulators
        com.google.android.gms.location.LocationRequest locationRequest =
                com.google.android.gms.location.LocationRequest.create();
        locationRequest.setPriority(com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(0); // Update immediately
        locationRequest.setFastestInterval(0);
        locationRequest.setNumUpdates(1); // Just need one update

        com.google.android.gms.location.LocationCallback locationCallback =
                new com.google.android.gms.location.LocationCallback() {
                    @Override
                    public void onLocationResult(com.google.android.gms.location.LocationResult locationResult) {
                        fusedLocationClient.removeLocationUpdates(this);

                        if (locationResult != null && !locationResult.getLocations().isEmpty()) {
                            // Get the first location from the result
                            android.location.Location location = locationResult.getLocations().get(0);
                            currentLatitude = location.getLatitude();
                            currentLongitude = location.getLongitude();

                            Toast.makeText(CreateAdvertActivity.this,
                                    "Location found: " + currentLatitude + ", " + currentLongitude,
                                    Toast.LENGTH_SHORT).show();

                            // Set the location text
                            locationEditText.setText("Retrieved Location: " + currentLatitude + ", " + currentLongitude);

                            try {
                                android.location.Geocoder geocoder = new android.location.Geocoder(CreateAdvertActivity.this, Locale.getDefault());
                                List<android.location.Address> addresses = geocoder.getFromLocation(currentLatitude, currentLongitude, 1);
                                if (addresses != null && !addresses.isEmpty()) {
                                    android.location.Address address = addresses.get(0);

                                    // Build a proper address string
                                    StringBuilder addressStr = new StringBuilder();
                                    for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                                        addressStr.append(address.getAddressLine(i));
                                        if (i < address.getMaxAddressLineIndex()) {
                                            addressStr.append(", ");
                                        }
                                    }

                                    if (addressStr.length() > 0) {
                                        locationEditText.setText(addressStr.toString());
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            Toast.makeText(CreateAdvertActivity.this,
                                    "Could not get location from emulator, using fallback",
                                    Toast.LENGTH_SHORT).show();

                            // Get the location that was set in your screenshot
                            currentLatitude = -37.8452;
                            currentLongitude = 145.1067;
                            locationEditText.setText("Burwood, Victoria (Fallback location)");
                        }
                    }
                };

        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback, android.os.Looper.getMainLooper());

        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            fusedLocationClient.removeLocationUpdates(locationCallback);

            if (currentLatitude == 0 && currentLongitude == 0) {
                Toast.makeText(CreateAdvertActivity.this,
                        "Location request timed out, using fallback",
                        Toast.LENGTH_SHORT).show();

                currentLatitude = -37.8452;
                currentLongitude = 145.1067;
                locationEditText.setText("Burwood, Victoria (Fallback location)");
            }
        }, 5000); // 5 second timeout
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Location permission is required to use this feature", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                locationEditText.setText(place.getAddress());

                LatLng latLng = place.getLatLng();
                if (latLng != null) {
                    currentLatitude = latLng.latitude;
                    currentLongitude = latLng.longitude;
                }
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
                Toast.makeText(this, "Error: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
            }
        }
    }

    private void saveItem() {
        String type = lostRadioButton.isChecked() ? "Lost" : "Found";
        String name = nameEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String date = dateEditText.getText().toString().trim();
        String location = locationEditText.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            nameEditText.setError("Please enter a name");
            return;
        }

        if (TextUtils.isEmpty(phone)) {
            phoneEditText.setError("Please enter a phone number");
            return;
        }

        if (TextUtils.isEmpty(description)) {
            descriptionEditText.setError("Please enter a description");
            return;
        }

        if (TextUtils.isEmpty(location)) {
            locationEditText.setError("Please enter a location");
            return;
        }

        Item item = new Item(type, name, phone, description, date, location, currentLatitude, currentLongitude);

        // Save to database
        long id = databaseHelper.insertItem(item);

        if (id > 0) {
            Toast.makeText(this, "Item saved successfully", Toast.LENGTH_SHORT).show();
            finish(); // Go back to previous activity
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        } else {
            Toast.makeText(this, "Error saving item", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}