package com.example.enhancelostandfoundapp;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private DatabaseHelper databaseHelper;
    private List<Item> itemsList;
    private boolean showSingleItem;
    private int itemId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Items Map");
        }

        showSingleItem = getIntent().getBooleanExtra("show_single_item", false);
        itemId = getIntent().getIntExtra("item_id", -1);

        // Initialize database helper
        databaseHelper = new DatabaseHelper(this);

        if (showSingleItem && itemId != -1) {
            itemsList = new ArrayList<>();
            Item item = databaseHelper.getItem(itemId);
            if (item != null) {
                itemsList.add(item);
            }
        } else {
            // Get all items
            itemsList = databaseHelper.getAllItems();
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (itemsList.isEmpty()) {
            LatLng defaultLocation = new LatLng(-37.8136, 144.9631);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10));
            return;
        }

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        boolean hasValidCoordinates = false;

        for (Item item : itemsList) {
            // Skip items with invalid coordinates (0,0)
            if (item.getLatitude() == 0 && item.getLongitude() == 0) {
                continue;
            }

            LatLng position = new LatLng(item.getLatitude(), item.getLongitude());

            float markerColor = item.getType().equals("Lost") ?
                    BitmapDescriptorFactory.HUE_ORANGE :
                    BitmapDescriptorFactory.HUE_GREEN;

            mMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title(item.getName())
                    .snippet(item.getType() + ": " + item.getLocation())
                    .icon(BitmapDescriptorFactory.defaultMarker(markerColor)));

            builder.include(position);
            hasValidCoordinates = true;
        }

        if (hasValidCoordinates) {
            if (showSingleItem && itemsList.size() == 1) {
                Item item = itemsList.get(0);
                LatLng position = new LatLng(item.getLatitude(), item.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15));

                mMap.setOnMapLoadedCallback(() -> {
                    com.google.android.gms.maps.model.Marker marker = mMap.addMarker(new MarkerOptions()
                            .position(position)
                            .title(item.getName())
                            .snippet(item.getType() + ": " + item.getLocation())
                            .icon(BitmapDescriptorFactory.defaultMarker(
                                    item.getType().equals("Lost") ?
                                            BitmapDescriptorFactory.HUE_ORANGE :
                                            BitmapDescriptorFactory.HUE_GREEN)));

                    if (marker != null) {
                        marker.showInfoWindow();
                    }
                });
            } else {
                int padding = 100; // pixels

                LatLngBounds bounds = builder.build();
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
            }
        } else {
            LatLng defaultLocation = new LatLng(-37.8136, 144.9631);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10));
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