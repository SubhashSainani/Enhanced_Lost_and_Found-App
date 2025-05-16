package com.example.enhancelostandfoundapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class ItemDetailActivity extends AppCompatActivity {

    private TextView detailItemType;
    private TextView detailItemName;
    private TextView detailItemDate;
    private TextView detailItemDescription;
    private TextView detailItemLocation;
    private TextView detailContactPhone;
    private Button showOnMapButton;
    private Button removeButton;

    private DatabaseHelper databaseHelper;
    private Item item;
    private int itemId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Item Details");
        }

        databaseHelper = new DatabaseHelper(this);

        detailItemType = findViewById(R.id.detailItemType);
        detailItemName = findViewById(R.id.detailItemName);
        detailItemDate = findViewById(R.id.detailItemDate);
        detailItemDescription = findViewById(R.id.detailItemDescription);
        detailItemLocation = findViewById(R.id.detailItemLocation);
        detailContactPhone = findViewById(R.id.detailContactPhone);
        showOnMapButton = findViewById(R.id.showOnMapButton);
        removeButton = findViewById(R.id.removeButton);

        itemId = getIntent().getIntExtra("item_id", -1);
        if (itemId == -1) {
            Toast.makeText(this, "Error: Item not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadItemDetails();

        showOnMapButton.setOnClickListener(v -> {
            // Only show on map if we have valid coordinates
            if (item.getLatitude() != 0 || item.getLongitude() != 0) {
                Intent intent = new Intent(ItemDetailActivity.this, MapActivity.class);
                intent.putExtra("item_id", item.getId());
                intent.putExtra("show_single_item", true);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            } else {
                Toast.makeText(ItemDetailActivity.this, "Location coordinates not available", Toast.LENGTH_SHORT).show();
            }
        });

        removeButton.setOnClickListener(v -> showRemoveConfirmationDialog());
    }

    private void loadItemDetails() {
        item = databaseHelper.getItem(itemId);
        if (item == null) {
            Toast.makeText(this, "Error: Item not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        detailItemType.setText(item.getType().toUpperCase());
        GradientDrawable background = (GradientDrawable) detailItemType.getBackground();
        if (item.getType().equals("Lost")) {
            background.setColor(Color.parseColor("#FF5722")); // Orange for Lost
        } else {
            background.setColor(Color.parseColor("#4CAF50")); // Green for Found
        }

        detailItemName.setText(item.getName());
        detailItemDate.setText(item.getDate());
        detailItemDescription.setText(item.getDescription());
        detailItemLocation.setText(item.getLocation());
        detailContactPhone.setText(item.getPhone());

        // Only enable the Show on Map button if we have coordinates
        showOnMapButton.setEnabled(item.getLatitude() != 0 || item.getLongitude() != 0);
    }

    private void showRemoveConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Remove Item");
        builder.setMessage("Are you sure you want to remove this item? This action cannot be undone.");
        builder.setPositiveButton("Remove", (dialog, which) -> removeItem());
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void removeItem() {
        databaseHelper.deleteItem(itemId);
        Toast.makeText(this, "Item removed successfully", Toast.LENGTH_SHORT).show();
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
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